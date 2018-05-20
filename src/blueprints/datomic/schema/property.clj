(ns blueprints.datomic.schema.property
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :starcity/add-property-schema
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [name :string :fulltext]
      [description :string :fulltext]

      [cover-image-url :string]

      [internal-name :string :unique-identity :fulltext]
      [address :ref :component]

      [units :ref :many
       "The units that exist in this property."]
      [licenses :ref :many
       "The licenses that are available for this property."]
      [available-on :instant
       "The date that this property will come online."]

      [upcoming :string
       "The date that this property will come online."]))]))


(tds/defschema :starcity/add-property-license-schema
  (s/generate-schema
   [(s/schema
     property-license
     (s/fields
      [license :ref
       "Reference to a license for a specific property."]
      [base-price :float
       "The base price for this license at this property."]))]))


(tds/defschema :starcity/add-unit-schema
  (s/generate-schema
   [(s/schema
     unit
     (s/fields
      [name :string :fulltext
       "Name of the unit."]
      [description :string :fulltext
       "Description of the unit."]
      [price :float
       "Additional per-month price of this unit on top of monthly lease."]
      [available-on :instant
       "The date that this unit is available for lease."]
      [floor :long
       "The floor that this unit is on."]
      [dimensions :ref :component
       "The dimensions of this unit."]))

    (s/schema
     unit-dimension
     (s/fields
      [height :float
       "Height of unit in feet."]
      [width :float
       "Width of unit in feet."]
      [length :float
       "Length/depth of unit in feet."]))]))


(tds/defschema :schema/add-stripe-credentials-to-property-schema-9-8-16
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [managed-account-id :string
       "The id of the managed Stripe account associated with this property."]))]))


(tds/defschema :schema/improvements-11-20-16
  [:starcity/add-property-schema]
  [{:db/id               :property/units
    :db/isComponent      true
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :property/licenses
    :db/isComponent      true
    :db/index            true
    :db.alter/_attribute :db.part/db}])


(tds/defschema :schema.property/add-ops-fee-12-14-16
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [ops-fee :float
       "The percentage fee that Starcity Ops takes from payments to this
       property."]))]))


(tds/defschema :schema.property.unit/unit-improvements-1-13-17
  [:starcity/add-unit-schema]
  [{:db/id               :unit/name
    :db/unique           :db.unique/identity
    :db.alter/_attribute :db.part/db}])


(tds/defschema :schema.property.unit/add-unit-licenses-and-number
  [:starcity/add-unit-schema]
  (s/generate-schema
   [(s/schema
     unit
     (s/fields
      [licenses :ref :many :component
       "Reference to the priced licenses for this unit."]
      [number :long :index
       "This unit's number."]))]))


(tds/defschema :schema.property/make-property-licenses-generic
  [:starcity/add-property-license-schema]
  [{:db/id               :property-license/license
    :db/ident            :license-price/license
    :db/index            true
    :db/doc              "The license."
    :db.alter/_attribute :db.part/db}
   {:db/id               :property-license/base-price
    :db/ident            :license-price/price
    :db/index            true
    :db/doc              "The price."
    :db.alter/_attribute :db.part/db}])


(tds/defschema :schema.property/add-tours-attr-03232017
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [tours :boolean
       "Indicates whether or not tours are currently being accepted for this
       property."]))]))


(tds/defschema :schema.property/improvements-07192017
  [:starcity/add-property-schema
   :schema/add-stripe-credentials-to-property-schema-9-8-16
   :schema.property/add-tours-attr-03232017]
  (concat
   (s/generate-schema
    [(s/schema
      property
      (s/fields
       [deposit-connect-id :string :indexed
        "The id of the Stripe Connect account used for security deposits."]))])

   [{:db/id  :property/cover-image-url
     :db/doc "DEPRECATED: No longer used."}
    {:db/id    :property/internal-name
     :db/ident :property/code
     :db/doc   "Internal code for looking up the property."}
    {:db/id    :property/available-on
     :db/index true}
    {:db/id    :property/managed-account-id
     :db/ident :property/rent-connect-id
     :db/index true
     :db/doc   "The id of the Stripe Connect account used for rent payments."}
    {:db/id    :property/ops-fee
     :db/index true}
    {:db/id    :property/tours
     :db/index true}]))


(tds/defschema :schema.property/ops-fee-extensions-11272017
  [:schema.property/add-ops-fee-12-14-16]
  (concat
   (s/generate-schema
    [(s/schema
      property
      (s/fields
       [ops-fee-orders :float :indexed
        "The percent fee to apply to premium service order payments."]))])
   [{:db/id               :property/ops-fee
     :db/ident            :property/ops-fee-rent
     :db/doc              "The percent fee to apply to rent payments."
     :db.alter/_attribute :db.part/db}]))


(tds/defschema :schema.property/rename-to-community-05202018
  [:starcity/add-property-schema
   :schema.property/add-tours-attr-03232017]
  [{:db/id    :property/name
    :db/ident :community/name}
   {:db/id    :property/description
    :db/ident :community/description}
   {:db/id    :property/code
    :db/ident :community/code}
   {:db/id    :property/address
    :db/ident :community/address}
   {:db/id    :property/units
    :db/ident :community/units}
   {:db/id    :property/available-on
    :db/ident :community/available-on}
   {:db/id    :property/tours
    :db/ident :community/tours}
   {:db/id    :property/cover-image-url
    :db/ident :community/cover-image-url}])
