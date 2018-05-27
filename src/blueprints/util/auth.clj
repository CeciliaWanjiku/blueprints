(ns blueprints.util.auth
  (:require [buddy.auth :as buddy]
            [ring.util.response :as response]
            [taoensso.timbre :as timbre]))

(defn unauthorized-handler
  [request metadata]
  (let [config (get-in request [:deps :config])]
    (let [[status msg] (if (buddy/authenticated? request)
                         [403 "You are not authorized to access this resource."]
                         [401 "You are not authenticated; please log in."])]
      (-> (response/response {:message msg})
          (response/status status)
          (response/content-type (:content-type request "application/json"))))))
