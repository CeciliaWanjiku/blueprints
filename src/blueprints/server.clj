(ns blueprints.server
  (:require [buddy.auth :as buddy]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [clojure.string :as string]
            [customs.access :as access]
            [mount.core :refer [defstate]]
            [blueprints.config :as config :refer [config]]
            [blueprints.datomic :refer [conn]]
            [blueprints.routes :as routes]
            [blueprints.teller :refer [teller]]
            [org.httpkit.server :as httpkit]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.datomic :refer [datomic-store session->entity]]
            [ring.util.response :as response]
            [taoensso.timbre :as timbre]
            [toolbelt.core :as tb]))

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
         :body   "Unexpected server error!"}))))


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


(defn- unauthorized-handler
  [request metadata]
  (let [config (get-in request [:deps :config])]
    (timbre/debug "(AUTH)REQUEST KEYS:" (keys request))
    (timbre/debug "(AUTH)HEADERS" (keys request))
    (let [[status msg] (if (buddy/authenticated? request)
                         [403 "You are not authorized to access this resource."]
                         [401 "You are not authenticated; please log in."])]
      (-> (response/response {:message msg})
          (response/status status)
          ;; TODO: use appropriate content type based on request
          (response/content-type "application/json")))))


(def ^:private auth-backend
  (access/auth-backend :unauthorized-handler unauthorized-handler))


(defn wrap-deps
  "Inject dependencies (`deps`) into the request."
  [handler deps]
  (fn [req]
    (handler (assoc req :deps deps))))


(defn app-handler [deps]
  (-> routes/routes
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)
      (wrap-deps deps)
      (wrap-logging)
      (wrap-keyword-params)
      (wrap-nested-params)
      (wrap-restful-format)
      (wrap-params)
      (wrap-multipart-params)
      (wrap-session {:store        (datomic-store (:conn deps) :session->entity session->entity)
                     :cookie-name  (config/cookie-name config)
                     :cookie-attrs {:secure (config/secure-sessions? config)}})
      (wrap-exception-handling)
      (wrap-content-type)))


;; =============================================================================
;; State
;; =============================================================================


(defn- start-server [port handler]
  (timbre/infof ::starting {:port port})
  (httpkit/run-server handler {:port port :max-body (* 20 1024 1024)}))


(defn- stop-server [server]
  (timbre/info ::stopping)
  (server))


(defstate web-server
  :start (->> (app-handler {:conn   conn
                            :config config
                            :teller teller})
              (start-server (config/webserver-port config)))
  :stop (stop-server web-server))
