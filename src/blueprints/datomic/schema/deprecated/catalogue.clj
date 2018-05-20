(ns blueprints.datomic.schema.deprecated.catalogue
  "NOTE: This schema is DEPRECATED and preserved for future reference."
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.catalogue/add-schema-04182017
  (concat
   (s/generate-schema
    [(s/schema
      catalogue
      (s/fields
       [name :string :fulltext
        "Human-readable name for this catalogue."]

       [code :keyword :indexed
        "Internal code for this catalogue."]

       [properties :ref :many :indexed
        "Properties that this catalogue pertains to. If unspecified, assumed to apply to all."]

       [items :ref :many :component :indexed
        "The service items that comprise this catalogue."]))

     (s/schema
      cat-item
      (s/fields
       [index :long :indexed
        "Placement of service within catalogue (ordering)."]

       [service :ref :indexed
        "The service."]

       [name :string :indexed
        "Override of service's name for display purposes."]

       [desc :string :indexed
        "Override of service's description for display purposes."]

       [fields :ref :many :component :indexed
        "Specifications for additional user input to collect."]))

     (s/schema
      cat-field
      (s/fields
       [label :string :indexed
        "Label for this field."]

       [type :ref :indexed "Type of field."]

       [key :keyword :indexed
        "Key for this field to identify its value by client."]

       [min :long "Minimum number."]
       [max :long "Maximum number."]
       [step :float "Number step."]))])

   [{:db/id    (tds/tempid)
     :db/ident :cat-field.type/desc}
    {:db/id    (tds/tempid)
     :db/ident :cat-field.type/quantity}]))
