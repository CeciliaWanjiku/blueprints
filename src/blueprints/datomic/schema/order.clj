(ns blueprints.datomic.schema.order
  "The `order` entity represents a the purchase of a `service` by an `account`."
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.order/add-schema-04132017
  (s/generate-schema
   [(s/schema
     order
     (s/fields
      [account :ref :indexed
       "The account that placed this order."]

      [uuid :uuid :unique-identity
       "UUID to identify this order."]

      [service :ref :indexed
       "The service that is being ordered."]

      [variant :ref :indexed
       "The service variant that was selected, if any."]

      [quantity :float :indexed
       "The number of `service` ordered."]

      [price :float :indexed
       "The price of a unit of this order. Used only in cases where a price cannot be specified on the service itself."]

      [desc :string :fulltext
       "Description of the order."]

      [ordered :instant :indexed
       "The instant in time at which this order was placed."]))]))


;;; The way orders (ordering) should work:

;; Order begins life as "pending" when it is created.
;; An order is transitioned to "placed" when it is ready to be charged.

;; An event will be issued (e.g. `:order/place`) that creates the charge and, if
;; successful, transitions the order status to "charged". From that point on,
;; the charge's status will be used for status determination.

(tds/defschema :schema.order/order-improvements-06292017
  (concat
   (s/generate-schema
    [(s/schema
      order
      (s/fields
       [payments :ref :many :indexed :component
        "Ref to payments associated with this order."]

       [status :ref :indexed
        "The status of this order."]))])

   [{:db/id    (tds/tempid)
     :db/ident :order.status/pending}
    {:db/id    (tds/tempid)
     :db/ident :order.status/placed}
    {:db/id    (tds/tempid)
     :db/ident :order.status/canceled}
    {:db/id    (tds/tempid)
     :db/ident :order.status/charged}]))


(tds/defschema :schema.order/add-processing-status-09132017
  [{:db/id    (tds/tempid)
    :db/ident :order.status/processing}])


(tds/defschema :schema.order/additions-10022017
  (concat
   (s/generate-schema
    [(s/schema
      order
      (s/fields

       [billed-on :instant :indexed
        "The date at which this order was billed."]

       [fulfilled-on :instant :indexed
        "The date at which this order was fulfilled."]

       [projected-fulfillment :instant :indexed
        "The date at which this order is projected to be fulfilled."]

       [cost :float :indexed
        "The cost of this order--used in absence of service cost or to override
        service cost."]))])

   [{:db/id    (tds/tempid)
     :db/ident :order.status/fulfilled}]))


(def ^{:added "1.14.0"} rename-desc-attr
  [{:db/id               :order/desc
    :db/ident            :order/request
    :db/doc              "Accompanying text with the order request, provided by user."
    :db.alter/_attribute :db.part/db}])


(tds/defschema :schema.order/additions-10222017
  [:schema.order/add-schema-04132017]
  (concat
   (s/generate-schema
    [(s/schema
      order
      (s/fields
       [summary :string :fulltext
        "Summary of the provided order by Starcity."]

       [lines :ref :many :indexed :component
        "Line-items attached to this order."]))

     (s/schema
      line-item
      (s/fields
       [desc :string :fulltext
        "Description of the line-item."]

       [cost :float :indexed
        "Cost of this line-item."]

       [price :float :indexed
        "Price of this line-item."]))])

   [{:db/id               :order/desc
     :db/ident            :order/request
     :db/doc              "Accompanying text with the order request, provided by user."
     :db.alter/_attribute :db.part/db}
    {:db/id    (tds/tempid)
     :db/ident :order.status/failed}]))


(tds/defschema :schema.order/add-order-fields-03192018
  (concat
   (s/generate-schema
    [(s/schema
      order
      (s/fields
       [fields :ref :indexed :many :component
        "Information collected from service fields while ordering"]))])

   [{:db/id                 (d/tempid :db.part/db)
     :db/ident              :order-field/service-field
     :db/valueType          :db.type/ref
     :db/cardinality        :db.cardinality/one
     :db/index              true
     :db/doc                "Reference to the service field that this order field is input for."
     :db.install/_attribute :db.part/db}
    {:db/id                 (d/tempid :db.part/db)
     :db/ident              :order-field.value/text
     :db/valueType          :db.type/string
     :db/cardinality        :db.cardinality/one
     :db/fulltext           true
     :db/doc                "A text value."
     :db.install/_attribute :db.part/db}
    {:db/id                 (d/tempid :db.part/db)
     :db/ident              :order-field.value/number
     :db/valueType          :db.type/float
     :db/cardinality        :db.cardinality/one
     :db/index              true
     :db/doc                "A float value."
     :db.install/_attribute :db.part/db}
    {:db/id                 (d/tempid :db.part/db)
     :db/ident              :order-field.value/date
     :db/valueType          :db.type/instant
     :db/cardinality        :db.cardinality/one
     :db/index              true
     :db/doc                "A date value."
     :db.install/_attribute :db.part/db}
    {:db/id                 (d/tempid :db.part/db)
     :db/ident              :order-field.value/option
     :db/valueType          :db.type/string
     :db/cardinality        :db.cardinality/one
     :db/index              true
     :db/doc                "A dropdown selection."
     :db.install/_attribute :db.part/db}]))


(tds/defschema :schema.order/add-subscriptions-reference-04102018
  (s/generate-schema
   [(s/schema
     order
     (s/fields
      [subscription :ref :indexed
       "Reference to a teller subscription."]))]))


(tds/defschema :schema.order/add-order-attached-04242018
  (s/generate-schema
   [(s/schema
     order
     (s/fields
      [attached :ref :indexed :many
       "A list of orders that this order references. Used to calculate fees
        based on the orders which generated the fee."]))]))
