(ns blueprints.datomic.schema.member-license
  (:require [datomic-schema.schema :as s]
            [toolbelt.datomic.schema :as tds]))

(tds/defschema :starcity/add-member-license-schema
  (s/generate-schema
   [(s/schema
     member-license
     (s/fields
      [license :ref
       "Reference to the license that this member has agreed to."]

      [price :float
       "The price of the member's license per month. This includes the base price
     of the license plus any additional fees, e.g. for pets."]

      [commencement-date :instant
       "The date that this license takes effect."]

      [end-date :instant
       "The date that this license ends."]))]))


(tds/defschema :schema.member-license/rent-alterations
  [:starcity/add-member-license-schema]
  (concat
   (s/generate-schema
    [(s/schema
      member-license
      (s/fields
       [active :boolean :index
        "Indicates whether or not this license is active. This is necessary
       because renewal results in creation of a new license."]

       [customer :ref :index
        "Reference to the managed :stripe-customer entity."]

       [plan-id :string :unique-identity
        "The id of the plan."]

       [subscription-id :string :unique-identity
        "The id of the subscription in Stripe that does the rent billing."]

       [unit :ref :index
        "Reference to the unit that the holder of this member license lives in."]))])

   [{:db/id               :member-license/commencement-date
     :db/ident            :member-license/commencement
     :db/index            true
     :db.alter/_attribute :db.part/db}
    {:db/id               :member-license/license
     :db/index            true
     :db.alter/_attribute :db.part/db}
    {:db/id               :member-license/price
     :db/ident            :member-license/rate
     :db/index            true
     :db.alter/_attribute :db.part/db}
    {:db/id               :member-license/end-date
     :db/ident            :member-license/ends
     :db/index            true
     :db.alter/_attribute :db.part/db}]))


(tds/defschema :schema.member-license/improvements-02162017
  [:schema.member-license/rent-alterations]
  (concat
   (s/generate-schema
    [(s/schema
      member-license
      (s/fields
       [status :ref :index
        "The status of this member license: active, inactive, renewal, canceled"]

       [move-out :boolean :index
        "Is this member moving out? Its presence (= true) tells us NOT to auto-renew the license."]))])

   [{:db/id    (tds/tempid)
     :db/ident :member-license.status/active}
    {:db/id    (tds/tempid)
     :db/ident :member-license.status/inactive}
    {:db/id    (tds/tempid)
     :db/ident :member-license.status/renewal}
    {:db/id    (tds/tempid)
     :db/ident :member-license.status/canceled}

    {:db/id               :member-license/active
     :db/doc "DEPRECATED 2/16/17: Use `:member-license.status/active` instead."
     :db.alter/_attribute :db.part/db}]))


(tds/defschema :schema.member-license/improvements-05202018
  [:schema.member-license/improvements-02162017
   :schema.member-license/rent-alterations]
  [{:db/id    :member-license/commencement
    :db/ident :member-license/starts}
   {:db/id               :member-license/unit
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :member-license/status
    :db/index            true
    :db.alter/_attribute :db.part/db}])


;; ==============================================================================
;; DEPRECATED ===================================================================
;; ==============================================================================


(tds/defschema :schema.member-license/add-rent-payments
  (concat
   (s/generate-schema
    [(s/schema
      member-license
      (s/fields
       [rent-payments :ref :many :index :component
        "References the rent payments that have been made for this license by
        owner."]))

     (s/schema
      rent-payment
      (s/fields
       [method :ref :index
        "The method of payment."]

       [status :ref :index
        "The status of this payment."]

       [amount :float :index
        "The amount in dollars that was paid."]

       [period-start :instant :index
        "The start date of this payment period."]

       [period-end :instant :index
        "The end date of this payment period."]

       [due-date :instant :index
        "The due date for this payment."]

       [paid-on :instant :index
        "Date that this payment was successfully paid on."]

       [check :ref :component
        "The associated check entity, in the event that `method` is `check`."]

       [charge :ref :component
        "The associated charge entity, in the event that `method` is `ach`."]

       [method-desc :string :fulltext
        "Description of the payment method, in the event that `method` is `other`."]

       [notes :ref :fulltext :many :component
        "Reference to any notes that have been added to this payment."]

       [invoice-id :string :unique-identity
        "The id of the Stripe Invoice if this payment is made with autopay."]

       [autopay-failures :long :index
        "The number of times that this payment has failed through autopay."]))])

   [{:db/id    (tds/tempid)
     :db/ident :rent-payment.method/check}
    {:db/id    (tds/tempid)
     :db/ident :rent-payment.method/autopay}
    {:db/id    (tds/tempid)
     :db/ident :rent-payment.method/ach}
    {:db/id    (tds/tempid)
     :db/ident :rent-payment.method/other}

    {:db/id    (tds/tempid)
     :db/ident :rent-payment.status/due}
    {:db/id    (tds/tempid)
     :db/ident :rent-payment.status/pending}
    {:db/id    (tds/tempid)
     :db/ident :rent-payment.status/paid}]))
