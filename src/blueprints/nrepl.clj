(ns blueprints.nrepl
  (:require [blueprints.config :as config :refer [config]]
            [clojure.tools.nrepl.server :refer [start-server stop-server]]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :as timbre]))

(defn- start-nrepl [port]
  (timbre/info ::starting {:port port})
  (start-server :port port))


(defstate nrepl
  :start (start-nrepl (config/nrepl-port config))
  :stop  (stop-server nrepl))
