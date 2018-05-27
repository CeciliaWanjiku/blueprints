(ns blueprints.datomic
  (:require [blueprints.config :as config :refer [config]]
            [blueprints.datomic.schema :as schema]
            [clojure.string :as string]
            [datomic.api :as d]
            [io.rkn.conformity :as conformity]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as timbre]
            [toolbelt.datomic.schema :as tds]))

(defn- scrub-uri [uri]
  (string/replace uri #"password.*" ""))


(defn new-connection [uri part]
  (timbre/info ::connecting {:uri (scrub-uri uri)})
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (tds/set-partition! part)
    (schema/conform-schema conn part)
    conn))


(defn disconnect [uri conn]
  (timbre/info ::disconnecting {:uri (scrub-uri uri)})
  (d/release conn))


(defstate conn
  :start (new-connection (config/datomic-uri config)
                         (config/datomic-partition config))
  :stop (disconnect (config/datomic-uri config) conn))
