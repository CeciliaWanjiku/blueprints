(ns blueprints.datomic.schema.security-deposit
  (:require [datomic-schema.schema :as s]
            [datomic.api :as d]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :schema/add-security-deposit-schema-8-18-16
  (concat
   (s/generate-schema
    [(s/schema
      security-deposit
      (s/fields
       [account :ref
        "Account with which this security deposit is associated."]

       [amount-received :long
        "Amount of money that has been received for this security deposit in cents."]

       [amount-required :long
        "Amount of money that is needed for this security deposit in cents."]

       [payment-method :ref
        "Method of payment for security deposit."]

       [payment-type :ref
        "Method of payment for security deposit."]

       [due-by :instant
        "Datetime by which security deposit must be paid."]

       [charge :ref
        "Reference to the Stripe charge entity in event of ACH."]

       [check-cleared? :boolean
        "Flag for us to reflect successful clearance of check (when applicable)."]))])

   [{:db/id    (tds/tempid)
     :db/ident :security-deposit.payment-method/ach}
    {:db/id    (tds/tempid)
     :db/ident :security-deposit.payment-method/check}
    {:db/id    (tds/tempid)
     :db/ident :security-deposit.payment-type/partial}
    {:db/id    (tds/tempid)
     :db/ident :security-deposit.payment-type/full}]))


(tds/defschema :schema/add-checks-to-security-deposit-schema-11-4-16
  (s/generate-schema
   [(s/schema
     security-deposit
     (s/fields
      [checks :ref :many :component
       "Any checks that have been received to pay this security deposit."]))]))


(tds/defschema :schema/alter-security-deposit-schema-11-2-16
  [:schema/add-security-deposit-schema-8-18-16]
  [{:db/id               :security-deposit/charge
    :db/ident            :security-deposit/charges
    :db/cardinality      :db.cardinality/many
    :db.alter/_attribute :db.part/db}])


(tds/defschema :schema.security-deposit/improve-charges-attr-02-14-17
  [:schema/alter-security-deposit-schema-11-2-16]
  [{:db/id               :security-deposit/charges
    :db/isComponent      true
    :db/index            true
    :db.alter/_attribute :db.part/db}])


(tds/defschema :schema.security-deposit/schema-overhaul-07202017
  [:schema/add-security-deposit-schema-8-18-16
   :schema/add-checks-to-security-deposit-schema-11-4-16
   :schema.security-deposit/improve-charges-attr-02-14-17]
  (concat
   (s/generate-schema
    [(s/schema
      deposit
      (s/fields
       [payments :ref :many :component :indexed
        "Any payments that have been made towards this security deposit."]
       [amount :float :indexed
        "The total amount that is required to be paid."]))])

   [;; rename and index
    {:db/id    :security-deposit/account
     :db/ident :deposit/account
     :db/index true}

    {:db/id    :security-deposit/payment-method
     :db/ident :deposit/method
     :db/index true}
    {:db/id    :security-deposit.payment-method/ach
     :db/ident :deposit.method/ach}
    {:db/id    :security-deposit.payment-method/check
     :db/ident :deposit.method/check}

    {:db/id    :security-deposit/payment-type
     :db/ident :deposit/type
     :db/index true}
    {:db/id    :security-deposit.payment-type/full
     :db/ident :deposit.type/full}
    {:db/id    :security-deposit.payment-type/partial
     :db/ident :deposit.type/partial}

    {:db/id    :security-deposit/due-by
     :db/ident :deposit/due
     :db/index true}

    ;; deprecations
    {:db/id  :security-deposit/amount-received
     :db/doc "DEPRECATED: Use `:deposit/payments` instead."}
    {:db/id  :security-deposit/amount-required
     :db/doc "DEPRECATED: Use `:deposit/amount` instead."}
    {:db/id  :security-deposit/check-cleared?
     :db/doc "DEPRECATED: What a terrible attribute. Geeze."}
    {:db/id  :security-deposit/charges
     :db/doc "DEPRECATED: Use `deposit/payments` instead."}
    {:db/id  :security-deposit/checks
     :db/doc "DEPRECATED: Use `deposit/payments` instead."}]))


(tds/defschema :schema.security-deposit/add-refund-status-07272017
  (concat
   (s/generate-schema
    [(s/schema
      deposit
      (s/fields
       [refund-status :ref :indexed
        "The status of the security deposit's refund."]))])
   [{:db/id    (tds/tempid)
     :db/ident :deposit.refund-status/initiated}
    {:db/id    (tds/tempid)
     :db/ident :deposit.refund-status/successful}
    {:db/id    (tds/tempid)
     :db/ident :deposit.refund-status/failed}]))
