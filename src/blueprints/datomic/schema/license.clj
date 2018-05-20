(ns blueprints.datomic.schema.license
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :starcity/add-license-schema
  (s/generate-schema
   [(s/schema
     license
     (s/fields
      [term :long
       "The term of the license in months."]))]))


(tds/defschema :license.schema/add-available-1-2-17
  (s/generate-schema
   [(s/schema
     license
     (s/fields
      [available :boolean
       "Whether or not this license is available for new applicants."]))]))


(tds/defschema :schema.license/add-indices-05202018
  [{:db/id    :license/term
    :db/index true}
   {:db/id    :license/available
    :db/index true}])
