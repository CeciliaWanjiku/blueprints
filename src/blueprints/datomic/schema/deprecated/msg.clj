(ns blueprints.datomic.schema.deprecated.msg
  "NOTE: This schema is DEPRECATED and preserved for future reference."
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.msg/add-msg-schema
  (s/generate-schema
   [(s/schema
     msg
     (s/fields
      [uuid :uuid :unique-identity
       "SQUUID to identify this msg."]

      [key :keyword
       "App-defined keyword to dispatch upon."]

      [params :bytes
       "Serialized parameters for the msg handler."]))]))


;; The choice to use `:bytes` as the `:db.valueType` for `:msg/params`has proven
;; to be a poor one. Reasons:

;; 1. Requires an external library to use (nippy).
;; 2. Params aren't human-readable without library.
;; 3. (1) and (2) together make the use of database functions difficult.

;; This schema change adds one more attribute that uses strings as the
;; serialization format, thus solving above issues.
(tds/defschema :schema.msg/improvements-05252017
  [:schema.msg/add-msg-schema]
  (concat
   (s/generate-schema
    [(s/schema
      msg
      (s/fields
       [data :string
        "The serialized data for the message handler."]))])

   [{:db/id  :msg/params
     :db/doc "DEPRECATED 1.6.0: Prefer `:msg/data`."}
    {:db/id               :msg/key
     :db/index            true
     :db.alter/_attribute :db.part/db}]))


(tds/defschema :schema.msg/add-create-msg-05252017
  [{:db/id    (tds/tempid)
    :db/ident :db.msg/create
    :db/doc   "Create a new msg."
    :db/fn
    (datomic.function/construct
     {:lang     "clojure"
      :params   '[db key data]
      :requires '[[datomic.api :as d]]
      :code     '[(merge
                   {:db/id    (d/tempid :db.part/starcity)
                    :msg/uuid (d/squuid)
                    :msg/key  key}
                   (when-not (empty? data) {:msg/data (pr-str data)}))]})}])
