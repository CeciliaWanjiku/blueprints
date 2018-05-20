(ns blueprints.datomic.schema.address
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :starcity/add-address-schema
  (s/generate-schema
   [(s/schema
     address
     (s/fields
      [lines :string "Address lines, separated by newlines."]
      [state :string]
      [city :string]
      [postal-code :string]))]))


(tds/defschema :schema/alter-address-schema-10-8-16
  [:starcity/add-address-schema]
  [{:db/id    :address/city
    :db/ident :address/locality
    :db/doc   "City/town"}
   {:db/id    :address/state
    :db/ident :address/region
    :db/doc   "State/province/region."}
   {:db/id                 #db/id[:db.part/db]
    :db/ident              :address/country
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/doc                "Country"
    :db.install/_attribute :db.part/db}])
