(ns blueprints.datomic.schema.deprecated.charge
  "NOTE: This schema is DEPRECATED and preserved for future reference."
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :starcity/add-charge-schema
  (s/generate-schema
   [(s/schema
     charge
     (s/fields
      [stripe-id :string :unique-identity
       "The Stripe ID for this charge."]

      [account :ref
       "The account with which this charge is associated."]

      [purpose :string :fulltext
       "Description of the purpose of this charge."]))]))


(tds/defschema :schema/add-charge-status-11-1-16
  (concat
   (s/generate-schema
    [(s/schema
      charge
      (s/fields
       [status :ref "The status of this charge."]))])

   [{:db/id    (tds/tempid)
     :db/ident :charge.status/pending}
    {:db/id    (tds/tempid)
     :db/ident :charge.status/succeeded}
    {:db/id    (tds/tempid)
     :db/ident :charge.status/failed}]))


(tds/defschema :schema.charge/add-charge-amount-2-1-17
  (s/generate-schema
   [(s/schema
     charge
     (s/fields
      [amount :float :index
       "The amount in dollars that the charge is for."]))]))
