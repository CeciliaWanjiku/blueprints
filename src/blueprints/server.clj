(ns blueprints.server
  (:require [blueprints.config :as config]
            [blueprints.routes :as routes]
            [blueprints.util.auth :refer [unauthorized-handler]]
            [buddy.auth.backends.token :refer [token-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [customs.access :as access]
            [datomic.api :as d]
            [org.httpkit.server :as httpkit]
            [ring.middleware.content-type :refer [wrap-content-type]]
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


(def tokens
  (atom {#uuid "5f89c319-eb62-4bdc-938c-a8e0a3a8f6ca" "admin@test.com"
         #uuid "c3330689-9c6e-4a1e-9991-98a2c0d1abcc" "applicant@test.com"}))


(defn token-authfn
  [req token]
  (try
    (when-let [email (get @tokens (java.util.UUID/fromString token))]
      (d/entity (d/db (get-in req [:deps :conn])) [:account/email email]))
    (catch Throwable _ nil)))


(comment

  @(org.httpkit.client/get "http://localhost:8083/history/17592186045726"
                           {:headers {"Authorization" (str "Token " #uuid "5f89c319-eb62-4bdc-938c-a8e0a3a8f6ca")
                                      "Content-Type"  "application/transit+json"}})

  (:db/id (d/entity (d/db odin.datomic/conn) [:account/email "admin@test.com"]))

  )


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
    (handler (assoc req :deps deps))))


(defn app-handler [deps]
  (-> routes/routes
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend (token-backend {:authfn               token-authfn
                                                        :unauthorized-handler unauthorized-handler}))
      (wrap-deps deps)
      (wrap-logging)
      (wrap-keyword-params)
      (wrap-nested-params)
      (wrap-restful-format)
      (wrap-params)
      (wrap-multipart-params)
      (wrap-session {:store        (datomic-store (:conn deps) :session->entity session->entity)
                     :cookie-name  (config/cookie-name (:config deps))
                     :cookie-attrs {:secure (config/secure-sessions? (:config deps))}})
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
