(ns blueprints.schema.order
  "The `order` entity represents a the purchase of a `service` by an `account`."
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]))


(def ^{:added "1.5.0"} schema
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


(def ^{:added "1.8.0"} order-improvements
  (s/generate-schema
   [(s/schema
     order
     (s/fields
      [payments :ref :many :indexed :component
       "Ref to payments associated with this order."]

      [status :ref :indexed
       "The status of this order."]))]))


;;; The way orders (ordering) should work:

;; Order begins life as "received" if it cannot be immediately
;; An order is transitioned to "placed" when it is ready to be charged.

;; An event will be issued (e.g. `:order/place`) that creates the charge and, if
;; successful, transitions the order status to "charged". From that point on,
;; the charge's status will be used for status determination.


(defn- ^{:added "1.8.0"} add-order-statuses [part]
  [{:db/id    (d/tempid part)
    :db/ident :order.status/pending}
   {:db/id    (d/tempid part)
    :db/ident :order.status/canceled}
   {:db/id    (d/tempid part)
    :db/ident :order.status/placed}
   {:db/id    (d/tempid part)
    :db/ident :order.status/charged}])


(defn norms [part]
  {:schema.order/add-schema-04132017
   {:txes [schema]}

   :schema.order/order-improvements-06292017
   {:txes [order-improvements (add-order-statuses part)]}})
