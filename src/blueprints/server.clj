(ns blueprints.server
  (:require [blueprints.config :as config]
            [blueprints.routes :as routes]
            [blueprints.models.api-key :as api-key]
            [blueprints.util.auth :refer [unauthorized-handler]]
            [buddy.auth.backends.token :refer [token-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [customs.access :as access]
            [datomic.api :as d]
            [org.httpkit.server :as httpkit]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.datomic :refer [datomic-store session->entity]]
            [taoensso.timbre :as timbre]
            [toolbelt.core :as tb]))

;; ==============================================================================
;; auth =========================================================================
;; ==============================================================================


(def ^:private auth-backend
  (access/auth-backend :unauthorized-handler unauthorized-handler))


(defn token-authfn
  [req token]
  (let [db (d/db (get-in req [:deps :conn]))]
    (try
      (when-let [key (api-key/by-id db (java.util.UUID/fromString token))]
        (or (api-key/account key)
            ;; TODO: Ideally there'll be an abstracted datastructure
            ;; representing the current `identity` that isn't coupled to the
            ;; notion of an "account"
            {:api-key/id         key
             :account/email      ""
             :account/first-name (api-key/name key)
             :account/last-name  ""
             :account/role       (api-key/role key)}))
      (catch Throwable _ nil))))


;; ==============================================================================
;; middleware ===================================================================
;; ==============================================================================


(defn wrap-exception-handling
  [handler]
  (fn [{:keys [session uri request-method remote-addr] :as req}]
    (try
      (handler req)
      (catch Throwable t
        (timbre/error t ::error (tb/assoc-when
                                 {:uri         uri
                                  :method      request-method
                                  :remote-addr remote-addr}
                                 :user (get-in session [:identity :account/email])))
        {:status 500
         :body   {:message "unexpected server error!"}}))))


(defn wrap-logging
  "Middleware to log requests."
  [handler]
  (fn [{:keys [deps params uri request-method session remote-addr] :as req}]
    (timbre/info "IDENTITY -------------------------- " (:identity session))
    (timbre/info :web/request
                 (tb/assoc-when
                  {:uri         uri
                   :method      request-method
                   :remote-addr remote-addr}
                  :user (get-in session [:identity :account/email])
                  :params (when-not (config/production? (:config deps)) params)))
    (handler req)))


(defn wrap-deps
  "Inject dependencies (`deps`) into the request."
  [handler deps]
  (fn [req]
    (timbre/info "SESSION --------------------------- " (:session req))
    (handler (assoc req :deps deps))))


(defn app-handler [deps]
  (-> routes/routes
      ;; #_(re-pattern (config/root-domain odin.config/config #_(:config deps)))
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend
                           (token-backend {:authfn               token-authfn
                                           :unauthorized-handler unauthorized-handler}))
      ;; NOTE: This works, but the authentication isn't working
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete :options])
      (wrap-deps deps)
      (wrap-logging)
      (wrap-keyword-params)
      (wrap-nested-params)
      (wrap-restful-format)
      (wrap-params)
      (wrap-multipart-params)
      (wrap-session {:store        (datomic-store (:conn deps) :session->entity session->entity)
                     :cookie-name  (config/session-name (:config deps))
                     :cookie-attrs {:secure (config/secure-sessions? (:config deps))
                                    :domain (config/session-domain (:config deps))}})
      (wrap-exception-handling)
      (wrap-content-type)))


;; ==============================================================================
;; server =======================================================================
;; ==============================================================================


(defn start-server
  [conn teller graphql config port]
  (let [handler (app-handler {:conn    conn
                              :config  config
                              :teller  teller
                              :graphql graphql})]
    (timbre/info ::starting {:port port})
    (httpkit/run-server handler {:port     port
                                 :max-body (* 20 1024 1024)})))


(defn stop-server [server]
  (timbre/info ::stopping)
  (server))
