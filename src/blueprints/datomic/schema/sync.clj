(ns blueprints.datomic.schema.sync
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.sync/add-sync-schema-01082018
  (s/generate-schema
   [(s/schema
     sync
     (s/fields
      [ref :ref :indexed
       "The entity that is being synced."]
      [ext-id :string :indexed :unique-identity
       "The id of the external representation of the synced entity (`ref`)."]
      [service :keyword :indexed
       "The service that `ref` is being synced with."]
      [last-synced :instant :indexed
       "The time at which the referenced entity was last synced."]))]))
