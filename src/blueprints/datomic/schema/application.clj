(ns blueprints.datomic.schema.application
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

;; ==============================================================================
;; application ==================================================================
;; ==============================================================================


(tds/defschema :starcity/add-member-application-schema
  (s/generate-schema
   [(s/schema
     member-application
     (s/fields
      [desired-properties :ref :many
       "Properties that applicant is interested in."]

      [desired-license :ref :component
       "License that this applicant wants."]

      [desired-availability :instant
       "Date that applicant would like to move in."]

      [pet :ref :component]

      [community-fitness :ref :component
       "The community fitness questionnaire."]

      [current-address :ref :component
       "Applicant's current address."]

      [locked :boolean
       "Indicates whether or not the application is locked for edits."]

      [approved :boolean
       "Indicates whether or not the application has been approved or not by an administrator."]

      [submitted-at :instant
       "The time at which the application was submitted."]))]))


(tds/defschema :starcity/add-pet-schema
  (s/generate-schema
   [(s/schema
     pet
     (s/fields
      [type :keyword "The type of pet."]
      [breed :string "The pet's breed."]
      [weight :long "The weight of the pet."]))]))


(tds/defschema :starcity/add-community-fitness-schema
  (s/generate-schema
   [(s/schema
     community-fitness
     (s/fields
      [prior-community-housing :string :fulltext
       "Response to: 'Have you ever lived in community housing?'"]

      [skills :string :fulltext
       "Response to: 'What skills or traits do you hope to share with the community?'"]

      [why-interested :string :fulltext
       "Response to: 'Why are you interested in Starcity?'"]

      [free-time :string :fulltext
       "Response to: 'How do you spend your free time'"]

      [dealbreakers :string :fulltext
       "Response to: 'Do you have an dealbreakers?'"]))]))


(tds/defschema :schema/add-has-pet-attr-10-3-16
  (s/generate-schema
   [(s/schema
     member-application
     (s/fields
      [has-pet :boolean
       "Whether or not applicant has a pet."]))]))


(tds/defschema :schema/add-member-application-status-11-15-16
  (concat
   (s/generate-schema
    [(s/schema
      member-application
      (s/fields
       [status :ref
        "The status of this member's application."]))])

   [{:db/id    (tds/tempid)
     :db/ident :member-application.status/in-progress}
    {:db/id    (tds/tempid)
     :db/ident :member-application.status/submitted}
    {:db/id    (tds/tempid)
     :db/ident :member-application.status/approved}
    {:db/id    (tds/tempid)
     :db/ident :member-application.status/rejected}]))


;; A variety of improvements to the application schema that add indices to all
;; attributes, improve naming, and fix some mistakes.
(tds/defschema :schema.member-application/improvements-11-20-16
  [:starcity/add-member-application-schema
   :schema/add-has-pet-attr-10-3-16
   :schema/add-member-application-status-11-15-16]
  [{:db/id               :member-application/desired-properties
    :db/ident            :member-application/properties
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :member-application/desired-license
    :db/ident            :member-application/license
    :db/index            true
    :db/isComponent      false
    :db.alter/_attribute :db.part/db}
   {:db/id               :member-application/desired-availability
    :db/ident            :member-application/move-in
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id  :member-application/locked
    :db/doc "DEPRECATED in favor of :member-application/status, 11/20/16"}
   {:db/id  :member-application/approved
    :db/doc "DEPRECATED in favor of :member-application/status, 11/20/16"}
   {:db/id  :member-application/submitted-at
    :db/doc "DEPRECATED 11/20/16"}])


(defn- rename-attr [[from to]]
  {:db/id               from
   :db/ident            to
   :db.alter/_attribute :db.part/db})


(defn- rename-attrs [& pairs]
  (assert (even? (count pairs)))
  (mapv rename-attr (partition 2 pairs)))


(tds/defschema :schema.member-application/naming-improvements-020417
  [:starcity/add-member-application-schema
   :starcity/add-community-fitness-schema
   :schema/add-has-pet-attr-10-3-16
   :schema/add-member-application-status-11-15-16
   :schema.member-application/improvements-11-20-16]
  (concat
   (rename-attrs
    :member-application/desired-properties :application/communities
    :member-application/desired-license :application/license
    :member-application/move-in :application/move-in
    :member-application/pet :application/pet
    :member-application/community-fitness :application/fitness
    :member-application/current-address :application/address
    :member-application/has-pet :application/has-pet
    :member-application/status :application/status)

   [{:db/id    :member-application.status/approved
     :db/ident :application.status/approved}
    {:db/id    :member-application.status/in-progress
     :db/ident :application.status/in-progress}
    {:db/id    :member-application.status/rejected
     :db/ident :application.status/rejected}
    {:db/id    :member-application.status/submitted
     :db/ident :application.status/submitted}]

   (rename-attrs
    :community-fitness/prior-community-housing :fitness/experience
    :community-fitness/skills :fitness/skills
    :community-fitness/free-time :fitness/free-time
    :community-fitness/why-interested :fitness/interested
    :community-fitness/dealbreakers :fitness/dealbreakers)))


(tds/defschema :schema.member-application/add-conflicts-to-fitness-05182017
  (s/generate-schema
   [(s/schema
     fitness
     (s/fields
      [conflicts :string :fulltext
       "How is applicant at resolving conflicts?"]))]))


(tds/defschema :schema.member-application/add-application-submitted-fn-05252017
  [{:db/id    (tds/tempid)
    :db/ident :db.application/submit
    :db/doc   "Submit a new member application."
    :db/fn
    (datomic.function/construct
     {:lang     "clojure"
      :params   '[db application-id]
      :requires '[[datomic.api :as d]]
      :code     '[{:db/id              application-id
                   :application/status :application.status/submitted}
                  [:db.cmd/create :application/submit {:data {:application-id application-id}}]
                  [:db.msg/create :application/submitted {:application-id application-id}]]})}])


(tds/defschema :schema.member-application.pets/add-attrs-06132017
  (s/generate-schema
   [(s/schema
     pet
     (s/fields
      [sterile :boolean
       "Has the pet been sterilized (spayed/neutered)?"]
      [vaccines :boolean
       "Are the pet's vaccines, licenses and inoculations current?"]
      [bitten :boolean
       "Has the pet ever bitten a human?"]
      [demeanor :string
       "Description of the pet's demeanor."]
      [daytime-care :string
       "Description of how the pet will be taken care of during the day."]))]))


;; ==============================================================================
;; community safety =============================================================
;; ==============================================================================


(tds/defschema :starcity/add-community-safety-schema
  (s/generate-schema
   [(s/schema
     community-safety
     (s/fields
      [account :ref
       "Account associated with this community safety information."]

      [report-url :string
       "API Location of the Community Safety info."]

      [wants-report? :boolean
       "Indicates whether or not this user wants a copy of their report."]))]))

(tds/defschema :schema/add-community-safety-consent-9-28-16
  (s/generate-schema
   [(s/schema
     community-safety
     (s/fields
      [consent-given? :boolean
       "Has user given us consent to perform a background check?"]))]))
