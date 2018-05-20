(ns blueprints.datomic.schema.transaction
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema.transaction/transaction-schema-01092018
  (s/generate-schema
   [(s/schema
     transaction
     (s/fields
      [source-id :string :unique-identity
       "Source-id of the balance transaction object."]
      [payment :ref :indexed
       "Reference to payment, if it exists."]
      [id :string :unique-identity
       "Id of the balance transaction."]
      [payout-id :string :unique-identity
       "Payout-id of the payout once it's been created."]))]))
