(ns blueprints.datomic.schema.session
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema/add-session-schema
  (s/generate-schema
   [(s/schema
     session
     (s/fields
      [key :string :unique-identity]
      [account :ref :index]
      [value :bytes :nohistory]))]))


(tds/defschema :schema.session/add-account-index-05202018
  [:schema/add-session-schema]
  [{:db/id    :session/account
    :db/index true}])
