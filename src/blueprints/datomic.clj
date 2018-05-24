(ns blueprints.datomic
  (:require [blueprints.config :as config :refer [config]]
            [blueprints.datomic.schema.account]
            [blueprints.datomic.schema.address]
            [blueprints.datomic.schema.approval]
            [blueprints.datomic.schema.application]
            [blueprints.datomic.schema.avatar]
            [blueprints.datomic.schema.event]
            [blueprints.datomic.schema.license]
            [blueprints.datomic.schema.member-license]
            [blueprints.datomic.schema.note]
            [blueprints.datomic.schema.onboard]
            [blueprints.datomic.schema.order]
            [blueprints.datomic.schema.property]
            [blueprints.datomic.schema.referral]
            [blueprints.datomic.schema.security-deposit]
            [blueprints.datomic.schema.service]
            [blueprints.datomic.schema.session]
            [blueprints.datomic.schema.source]
            [blueprints.datomic.schema.sync]
            [blueprints.datomic.schema.tag]
            [blueprints.datomic.schema.transaction]
            [blueprints.datomic.seed :as seed]
            [clojure.string :as string]
            [datomic.api :as d]
            [io.rkn.conformity :as conformity]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as timbre]
            [toolbelt.datomic.schema :as tds]))

(defn- partition-norms
  [part]
  {part {:txes [[{:db/id                 #db/id[:db.part/db]
                  :db/ident              part
                  :db.install/_partition :db.part/db}]]}})


(defn read-schema []
  (->> '[blueprints.datomic.schema.account
         blueprints.datomic.schema.address
         blueprints.datomic.schema.approval
         blueprints.datomic.schema.application
         blueprints.datomic.schema.avatar
         blueprints.datomic.schema.event
         blueprints.datomic.schema.license
         blueprints.datomic.schema.member-license
         blueprints.datomic.schema.note
         blueprints.datomic.schema.onboard
         blueprints.datomic.schema.order
         blueprints.datomic.schema.property
         blueprints.datomic.schema.referral
         blueprints.datomic.schema.security-deposit
         blueprints.datomic.schema.service
         blueprints.datomic.schema.session
         blueprints.datomic.schema.source
         blueprints.datomic.schema.sync
         blueprints.datomic.schema.tag
         blueprints.datomic.schema.transaction]
       (map create-ns)
       (apply tds/compile-schemas)))


(defn conform-schema
  ([conn]
   (conform-schema conn (config/datomic-partition config)))
  ([conn part]
   (conformity/ensure-conforms conn (partition-norms part))
   (tds/install-schema conn (read-schema))
   (seed/conform conn part)))


(defn- scrub-uri [uri]
  (string/replace uri #"password.*" ""))


(defn- new-connection [uri part]
  (timbre/info ::connecting {:uri (scrub-uri uri)})
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (tds/set-partition! part)
    (conform-schema conn part)
    conn))


(defn- disconnect [uri conn]
  (timbre/info ::disconnecting {:uri (scrub-uri uri)})
  (d/release conn))


(defstate conn
  :start (new-connection (config/datomic-uri config)
                         (config/datomic-partition config))
  :stop (disconnect (config/datomic-uri config) conn))
