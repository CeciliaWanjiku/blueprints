(ns blueprints.datomic.schema.account
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]
            [toolbelt.datomic.schema :as tds]))


;; added 1.0.0
(tds/defschema :starcity/add-account-schema
  (s/generate-schema
   [(s/schema
     account
     (s/fields
      [first-name :string :fulltext]
      [middle-name :string :fulltext]
      [last-name :string :fulltext]
      [phone-number :string]
      [email :string :unique-identity :fulltext]

      [password :string
       "User's hashed password."]

      [member-application :ref
       "The rental application associated with this account."]

      [unit :ref
       "The unit that the person identified by this account is living in."]

      [license :ref
       "The user's license."]

      [activation-hash :string
       "The user's activation hash, generated at the time of signup."]

      [activated :boolean
       "Becomes true after account activation."]

      [dob :instant
       "User's date of birth."]

      [role :enum]))]))


;; added 1.0.0
(tds/defschema :starcity/add-account-roles
  [{:db/id    (tds/tempid)
    :db/ident :account.role/applicant}
   {:db/id    (tds/tempid)
    :db/ident :account.role/tenant}
   {:db/id    (tds/tempid)
    :db/ident :account.role/admin}])


(tds/defschema :starcity/add-account-role-pending-8-18-16
  [{:db/id    (tds/tempid)
    :db/ident :account.role/pending}])


(tds/defschema :schema.account/add-indexes-11-20-16
  [:starcity/add-account-schema]
  [{:db/id               :account/first-name
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/middle-name
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/last-name
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/phone-number
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/member-application
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/unit
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/license
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/activation-hash
    :db/index            true
    :db.alter/_attribute :db.part/db}])


;; Our original language for roles (tenant/pending) is inaccurate -- rename them
;; to something more like the language we actually use (member/onboarding).
(tds/defschema :schema.account/rename-roles
  [:starcity/add-account-roles]
  [{:db/id    :account.role/tenant
    :db/ident :account.role/member}
   {:db/id    :account.role/pending
    :db/ident :account.role/onboarding}])


(tds/defschema :schema.account/license-alterations
  [:starcity/add-account-schema]
  [{:db/id               :account/license
    :db/cardinality      :db.cardinality/many
    :db/isComponent      true
    :db.alter/_attribute :db.part/db}])


(tds/defschema :schema.account/rename-license-to-licenses
  [:starcity/add-account-schema]
  [{:db/id    :account/license
    :db/ident :account/licenses
    :db/doc   "All licenses that belong to this account."}])


(tds/defschema :schema.account/rename-member-application
  [:starcity/add-account-schema]
  [{:db/id               :account/member-application
    :db/ident            :account/application
    :db.alter/_attribute :db.part/db}])


(tds/defschema :schema.account/schema-additions-02272017
  (s/generate-schema
   [(s/schema
     account
     (s/fields
      [notes :ref :many :component
       "Any notes attached to this account."]

      [slack-handle :string :unique-identity
       "This account's Slack handle."]))]))


(tds/defschema :schema.account/add-collaborator-role-032217
  [{:db/id    (tds/tempid)
    :db/ident :account.role/collaborator}])


(tds/defschema :schema.account/improvements-06142017
  [:starcity/add-account-schema]
  (concat
   ;; make person attributes more generic
   [{:db/id               :account/first-name
     :db/ident            :person/first-name
     :db.alter/_attribute :db.part/db}
    {:db/id               :account/middle-name
     :db/ident            :person/middle-name
     :db.alter/_attribute :db.part/db}
    {:db/id               :account/last-name
     :db/ident            :person/last-name
     :db.alter/_attribute :db.part/db}
    {:db/id               :account/phone-number
     :db/ident            :person/phone-number
     :db.alter/_attribute :db.part/db}]

   ;; add emergency contact info
   (s/generate-schema
    [(s/schema
      account
      (s/fields
       [emergency-contact :ref :component
        "Emergency contact information for this account."]))])))
