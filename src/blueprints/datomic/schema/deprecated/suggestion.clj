(ns blueprints.datomic.schema.deprecated.suggestion
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.suggestion/add-schema
  (s/generate-schema
   [(s/schema
     suggestion
     (s/fields
      [city :string :indexed
       "The city that was suggested for Starcity to expand to."]

      [account :ref :indexed
       "The (optional) account of the suggester."]))]))
