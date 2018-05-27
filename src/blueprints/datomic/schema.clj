(ns blueprints.datomic.schema
  (:require [blueprints.datomic.schema.account]
            [blueprints.datomic.schema.address]
            [blueprints.datomic.schema.approval]
            [blueprints.datomic.schema.application]
            [blueprints.datomic.schema.avatar]
            [blueprints.datomic.schema.auth]
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
            [blueprints.datomic.schema.source]
            [blueprints.datomic.schema.sync]
            [blueprints.datomic.schema.tag]
            [blueprints.datomic.schema.transaction]
            [blueprints.datomic.seed :as seed]
            [clojure.string :as string]
            [datomic.api :as d]
            [io.rkn.conformity :as conformity]
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
         blueprints.datomic.schema.auth
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
         blueprints.datomic.schema.source
         blueprints.datomic.schema.sync
         blueprints.datomic.schema.tag
         blueprints.datomic.schema.transaction]
       (map create-ns)
       (apply tds/compile-schemas)))


(defn conform-schema
  ([conn]
   (conform-schema conn :db.part/user))
  ([conn part]
   (conformity/ensure-conforms conn (partition-norms part))
   (tds/install-schema conn (read-schema))
   (seed/conform conn part)))
