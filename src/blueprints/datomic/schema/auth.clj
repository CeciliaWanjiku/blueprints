(ns blueprints.datomic.schema.auth
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


(tds/defschema :schema.auth/add-api-keys-05272018
  (s/generate-schema
   [(s/schema
     api-key
     (s/fields
      [id :uuid :unique-identity
       "The key itself."]

      [name :string :indexed
       "The name used to identify the owner/application that this key belongs to."]

      [account :ref :indexed
       "The account that this key is issued for, if any."]

      [role :ref :indexed
       "The role that this key acts with."]

      [issued :instant :indexed
       "The instant in time at which this key was issued."]

      [active :boolean :indexed
       "Is this key active?"]))]))
