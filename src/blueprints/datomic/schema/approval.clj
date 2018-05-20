(ns blueprints.datomic.schema.approval
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))


(tds/defschema :schema/add-approval-schema-9-8-16
  (s/generate-schema
   [(s/schema
     approval
     (s/fields
      [account :ref
       "The account that is being approved."]
      [approved-by :ref
       "Administrator that approved this account."]
      [approved-on :instant
       "Instant at which this approval was made."]
      [property :ref
       "The property that this account is being approved for."]))]))


(tds/defschema :schema.approval/improvements-02-17-16
  [:schema/add-approval-schema-9-8-16]
  (concat
   (s/generate-schema
    [(s/schema
      approval
      (s/fields
       [unit :ref :index
        "The unit that this account has been approved for."]
       [move-in :instant :index
        "The move-in date that this account has been approved for."]
       [license :ref :index
        "The license that this account has been approved for."]
       [status :ref :index
        "The status of the approval."]))])

   [{:db/id    (tds/tempid)
     :db/ident :approval.status/pending}
    {:db/id    (tds/tempid)
     :db/ident :approval.status/canceled}
    {:db/id    (tds/tempid)
     :db/ident :approval.status/approved}]

   [{:db/id               :approval/approved-on
     :db/doc "DEPRECATED 02/13/17 in favor of innate Datomic superpowers."
     :db.alter/_attribute :db.part/db}
    {:db/id               :approval/approved-by
     :db/ident            :approval/approver
     :db/index            true
     :db.alter/_attribute :db.part/db}
    {:db/id               :approval/account
     :db/index            true
     :db.alter/_attribute :db.part/db}
    {:db/id  :approval/property
     :db/doc "DEPRECATED 02/13/17 in favor of `:approval/unit`."}]))


(tds/defschema :schema.approval/index-attributes-05202018
  [:schema.approval/improvements-02-17-16]
  [{:db/id    :approval/unit
    :db/index true}
   {:db/id    :approval/move-in
    :db/index true}
   {:db/id    :approval/license
    :db/index true}
   {:db/id    :approval/status
    :db/index true}])
