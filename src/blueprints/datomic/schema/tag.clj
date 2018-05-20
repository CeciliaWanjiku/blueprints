(ns blueprints.datomic.schema.tag
  "NOTE: This schema is not currently in use anywhere."
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.tag/add-schema-02242017
  (s/generate-schema
   [(s/schema
     tag
     (s/fields
      [text :string :fulltext
       "The tag text itself."]

      [category :keyword :index
       "A keyword that categorizes this tag."]))]))
