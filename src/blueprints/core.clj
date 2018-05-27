(ns blueprints.core
  (:require [blueprints.config :as config :refer [config]]
            [blueprints.datomic :refer [conn]]
            [blueprints.log]
            [blueprints.nrepl]
            [blueprints.graphql :as graphql]
            [blueprints.teller :refer [teller]]
            [blueprints.server :as server]
            [mount.core :as mount :refer [defstate]]))

(defstate web-server
  :start (server/start-server conn
                              teller
                              (graphql/compile-schema)
                              config
                              (config/webserver-port config))
  :stop (server/stop-server web-server))
