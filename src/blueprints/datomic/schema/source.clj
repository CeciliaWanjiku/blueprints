(ns blueprints.datomic.schema.source
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.source/add-schema-10032017
  (s/generate-schema
   [(s/schema
     source
     (s/fields
      [account :ref :indexed
       "The account that initiated this transaction."]))]))
