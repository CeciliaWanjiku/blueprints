(ns blueprints.datomic.schema.deprecated.stripe-event
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))


(tds/defschema :schema/add-stripe-event-schema-11-1-16
  (concat
   (s/generate-schema
    [(s/schema
      stripe-event
      (s/fields
       [event-id :string :unique-identity
        "The Stripe ID for this event."]
       [type :string
        "The event's type."]
       [status :ref
        "The status of this event. One of #{:processing :succeeded :failed}"]))])

   [{:db/id    (tds/tempid)
     :db/ident :stripe-event.status/processing}
    {:db/id    (tds/tempid)
     :db/ident :stripe-event.status/succeeded}
    {:db/id    (tds/tempid)
     :db/ident :stripe-event.status/failed}]))
