(ns blueprints.datomic.schema.service
  "The `service` entity represents a Starcity offering with monetary value that
  can be purchased."
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.services/add-schema-04132017
  (concat
   (s/generate-schema
    [(s/schema
      service
      (s/fields
       [code :string :fulltext
        "Internal indentifier for this service."]

       [name :string :fulltext
        "Override of service's name for display purposes."]

       [desc :string :fulltext
        "External human-friendy description of this service."]

       [desc-internal :string :fulltext
        "Human-friendly description of this service for internal record-keeping."]

       [variants :ref :many :component :indexed
        "Variants of this service."]

       [price :float :indexed
        "Price of a unit of this service. Can be omitted for quote-based services."]

       [rental :boolean :indexed
        "`true` if this service represents a rental."]

       [properties :ref :many :indexed
        "Properties that this service pertains to. If unspecified, assumed to apply to all."]

       [billed :ref :indexed
        "Specifies the method in which this service should be billed."]))

     (s/schema
      svc-variant
      (s/fields
       [name :string :indexed "Name of this variant."]
       [price :float :indexed "Price override of the base service."]))])

   [{:db/id    (tds/tempid)
     :db/ident :service.billed/once}
    {:db/id    (tds/tempid)
     :db/ident :service.billed/monthly}]))


(tds/defschema :schema.service/add-cost-10202017
  (s/generate-schema
   [(s/schema
     service
     (s/fields
      [cost :float :indexed
       "The cost of this service."]))

    (s/schema
     svc-variant
     (s/fields
      [cost :float :indexed "Cost override of the base service."]))]))


(tds/defschema :schema.service/add-fields-and-catalogs-03012018
  (concat
   (s/generate-schema
    [(s/schema
      service
      (s/fields
       [catalogs :keyword :many :indexed
        "The catalogs in which this service should appear"]

       [fields :ref :many :component :indexed
        "A service's fields."]

       [active :boolean :indexed
        "`true` if the service is made available."]

       [name-internal :string :indexed
        "The staff-facing name for a service offering."]

       [fees :ref :many :indexed
        "One-time setup fees (like installation for furniture rentals) that
        are incurred when ordering a service. These are, themselves, services."]))

     (s/schema
      service-field
      (s/fields
       [index :long :indexed
        "The position of this field within the list"]


       [type :ref :indexed
        "The type of service field."]

       [label :string :indexed
        "The label for the input fields in the UI"]


       [required :boolean
        "`true` if the user is required to enter a value into this field when placing an order"]

       [options :ref :many :component :indexed
        "Options to choose from if the service field is of type `dropdown`"]))

     (s/schema
      service-field-option
      (s/fields
       [value :string :indexed
        "The actual value of the dropdown. Stored as a string."]
       [label :string :indexed
        "The label presented to the user."]
       [index :long :indexed
        "The position in which this option should appear within the dropdown menu."]))])


   [{:db/id    (tds/tempid)
     :db/ident :service-field.type/time}
    {:db/id    (tds/tempid)
     :db/ident :service-field.type/date}
    {:db/id    (tds/tempid)
     :db/ident :service-field.type/text}
    {:db/id    (tds/tempid)
     :db/ident :service-field.type/number}
    {:db/id    (tds/tempid)
     :db/ident :service-field.type/dropdown}]


   [{:db/id          (d/tempid :db.part/db)
     :db/ident       :service-field.time/range-start
     :db/valueType   :db.type/instant
     :db/cardinality :db.cardinality/one
     :db/index       true
     :db/doc         "The starting range of a time field."}
    {:db/id          (d/tempid :db.part/db)
     :db/ident       :service-field.time/range-end
     :db/valueType   :db.type/instant
     :db/cardinality :db.cardinality/one
     :db/index       true
     :db/doc         "The ending range of a time field."}
    {:db/id          (d/tempid :db.part/db)
     :db/ident       :service-field.time/interval
     :db/valueType   :db.type/long
     :db/cardinality :db.cardinality/one
     :db/index       true

     :db/doc "The interval of a time field in minutes."}
    {:db/id     :service/code
     :db/unique :db.unique/identity}]))


(tds/defschema :schema.service/add-types-04092018
  (concat
   (s/generate-schema
    [(s/schema
      service
      (s/fields
       [type :ref :indexed
        "Indicates the type of service (service, fee, event ticket, etc.)"]))])

   [{:db/id    (tds/tempid)
     :db/ident :service.type/service}
    {:db/id    (tds/tempid)
     :db/ident :service.type/fee}]))


(tds/defschema :schema.service/add-archive-04182018
  (s/generate-schema
   [(s/schema
     service
     (s/fields
      [archived :boolean :indexed
       "`true` if this service has been archived and will not be offered anymore"]))]))


(tds/defschema :schema.service/add-excluded-days-04192018
  [{:db/id          #db/id[:db.part/db]
    :db/ident       :service-field.date/excluded-days
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/many
    :db/index       true
    :db/doc         "A list of days of the week during which a service cannot
                     be fulfilled."}])


(tds/defschema :schema.service/add-plan-04102018
  (s/generate-schema
   [(s/schema
     service
     (s/fields
      [plan :ref :indexed
       "Reference to a teller plan."]))]))
