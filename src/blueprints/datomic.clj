(ns blueprints.datomic
  (:require [blueprints.config :as config :refer [config]]
            [blueprints.datomic.schema.account]
            [blueprints.datomic.schema.address]
            [blueprints.datomic.schema.approval]
            [blueprints.datomic.schema.avatar]
            [clojure.string :as string]
            [datomic.api :as d]
            [io.rkn.conformity :as conformity]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as timbre]
            [toolbelt.datomic.schema :as tds]))

(defn- partition-norms
  []
  {tds/*part*
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              tds/*part*
             :db.install/_partition :db.part/db}]]}})


(defn read-schema []
  (->> '[blueprints.datomic.schema.account
         blueprints.datomic.schema.address
         blueprints.datomic.schema.approval
         blueprints.datomic.schema.avatar]
       (map create-ns)
       (apply tds/compile-schemas)))


(defn conform-schema [conn]
  (conformity/ensure-conforms conn (partition-norms))
  (tds/install-schema conn (read-schema)))


(defstate conn
  :start (do
           (tds/set-partition! (config/datomic-partition config))
           (read-schema)))


(comment

  (read-schema)

  )
