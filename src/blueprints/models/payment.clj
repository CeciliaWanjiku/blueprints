(ns blueprints.models.payment
  (:require [datomic.api :as d]
            [toolbelt
             [datomic :as td]
             [predicates :as p]]
            [clojure.spec :as s]
            [blueprints.models.check :as check]
            [blueprints.models.member-license :as member-license]
            [toolbelt.date :as date]
            [clj-time.coerce :as c]
            [clj-time.core :as t]))


(def max-autopay-failures
  "The maximum number of times that autopay payments will be tried before the
  subscription is canceled."
  3)


;; =============================================================================
;; Specs
;; =============================================================================


(s/def ::method
  #{:payment.method/stripe-charge
    :payment.method/stripe-invoice
    :payment.method/check
    :payment.method/other})


(s/def ::status
  #{:payment.status/due
    :payment.status/canceled
    :payment.status/paid
    :payment.status/pending
    :payment.status/failed
    :payment.status/refunded})


(s/def ::for
  #{:payment.for/rent
    :payment.for/deposit
    :payment.for/order})


;; =============================================================================
;; Selectors
;; =============================================================================


(def id
  "The payment's unique id."
  :payment/id)

(s/fdef id
        :args (s/cat :payment p/entity?)
        :ret uuid?)


(def method
  "The method of payment."
  :payment/method)

(s/fdef method
        :args (s/cat :payment p/entity?)
        :ret (s/or :method ::method :nothing nil?))


(def amount
  "The amount to be paid."
  :payment/amount)

(s/fdef amount
        :args (s/cat :payment p/entity?)
        :ret float?)


(def status
  "The payment status."
  :payment/status)

(s/fdef status
        :args (s/cat :payment p/entity?)
        :ret ::status)


(def due
  "The due date for this payment."
  :payment/due)

(s/fdef due
        :args (s/cat :payment p/entity?)
        :ret (s/or :inst inst? :nothing nil?))


(def period-start
  "The period start for this payment."
  :payment/pstart)

(s/fdef period-start
        :args (s/cat :payment p/entity?)
        :ret (s/or :inst inst? :nothing nil?))


(def period-end
  "The period end for this payment."
  :payment/pend)

(s/fdef period-end
        :args (s/cat :payment p/entity?)
        :ret (s/or :inst inst? :nothing nil?))


(def paid-on
  "The date at which this payment was paid."
  :payment/paid-on)

(s/fdef paid-on
        :args (s/cat :payment p/entity?)
        :ret (s/or :inst inst? :nothing nil?))


(def account
  "The account that made this payment."
  :payment/account)

(s/fdef account
        :args (s/cat :payment p/entity?)
        :ret (s/or :entity p/entity? :nothing nil?))


(defn- infer-payment-for [payment]
  (cond
    (some? (:deposit/_payments payment))             :payment.for/deposit
    (some? (:order/_payments payment))               :payment.for/order
    (some? (:member-license/_rent-payments payment)) :payment.for/rent
    :otherwise                                       nil))


(defn payment-for [payment]
  "What this payment is for."
  (or (:payment/for payment)
      (infer-payment-for payment)))

(s/fdef payment-for
        :args (s/cat :payment p/entity?)
        :ret (s/or :for ::for :nothing nil?))


(defn charge-id
  "The id of the Stripe charge."
  [payment]
  (:stripe/charge-id payment))

(s/fdef charge-id
        :args (s/cat :payment p/entity?)
        :ret (s/or :string string? :nothing nil?))


(defn invoice-id
  "The id of the Stripe invoice."
  [payment]
  (:stripe/invoice-id payment))

(s/fdef invoice-id
        :args (s/cat :payment p/entity?)
        :ret (s/or :string string? :nothing nil?))


(defn source-id
  "The id of the Stripe payment source."
  [payment]
  (:stripe/source-id payment))

(s/fdef source-id
        :args (s/cat :payment p/entity?)
        :ret (s/or :string string? :nothing nil?))


(defn order
  "The order that this payment is associated with, if any."
  [db payment]
  (->> (d/q '[:find ?e .
              :in $ ?py
              :where
              [?e :order/payments ?py]]
            db (td/id payment))
       (d/entity db)))

(s/fdef order
        :args (s/cat :db p/db? :payment p/entity?)
        :ret (s/or :entity p/entityd? :nothing nil?))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn- has-method? [m payment]
  (= m (method payment)))

(s/fdef has-method?
        :args (s/cat :method ::method :payment p/entity?)
        :ret boolean?)


(defn charge?
  "Is this payment paid via a Stripe charge?"
  [payment]
  (has-method? :payment.method/stripe-charge payment))


(defn invoice?
  "Is this payment paid via a Stripe invoice?"
  [payment]
  (some? (invoice-id payment)))


(defn autopay?
  "Is this payment an autopay payment? A payment is an autopay payment if it has
  an associated invoice and it is attached to a rent payment."
  [payment]
  (and (invoice? payment) (= (payment-for payment) :payment.for/rent)))

(s/fdef autopay?
        :args (s/cat :payment p/entity?)
        :ret boolean?)


(defn check?
  "Is this payment paid via a check?"
  [payment]
  (has-method? :payment.method/check payment))


(defn- has-status? [m payment]
  (= m (status payment)))

(s/fdef has-status?
        :args (s/cat :method ::status :payment p/entity?)
        :ret boolean?)

(def paid?
  "Has this payment been paid?"
  (partial has-status? :payment.status/paid))


(def pending?
  "Is this payment pending?"
  (partial has-status? :payment.status/pending))

(s/fdef pending?
        :args (s/cat :payment p/entity?)
        :ret boolean?)


(def failed?
  "Has this payment failed to be charged?"
  (partial has-status? :payment.status/failed))

(s/fdef failed?
        :args (s/cat :payment p/entity?)
        :ret boolean?)


;; =============================================================================
;; Transactions
;; =============================================================================


(s/def ::account p/entity?)
(s/def ::uuid uuid?)
(s/def ::due inst?)
(s/def ::pstart inst?)
(s/def ::pend inst?)
(s/def ::paid-on inst?)
(s/def ::source-id string?)


(defn create
  "Create a new payment."
  [amount account & {:keys [uuid due for status method charge-id invoice-id
                            pstart pend paid-on source-id]
                     :or   {uuid   (d/squuid)
                            status :payment.status/pending}}]
  (when (= method :payment.method/stripe-charge)
    (assert (some? charge-id)
            "The charge id must be specified when the method is `stripe-charge`!"))
  (when (= method :payment.method/stripe-invoice)
    (assert (some? invoice-id)
            "The invoice id must be specified when the method is `stripe-invoice`!"))
  ;; TODO: The `method` seems unnecessary.
  (let [method (cond
                 (and (some? invoice-id) (nil? method)) :payment.method/stripe-invoice
                 (and (some? charge-id) (nil? method))  :payment.method/stripe-charge
                 :otherwise                             method)
        status (if (some? paid-on) :payment.status/paid status)]
    (toolbelt.core/assoc-when
     {:db/id           (d/tempid :db.part/starcity)
      :payment/id      uuid
      :payment/amount  amount
      :payment/account (td/id account)
      :payment/status  status}
     :stripe/invoice-id invoice-id
     :stripe/charge-id charge-id
     :stripe/source-id source-id
     :payment/method method
     :payment/due due
     :payment/for for
     :payment/pstart pstart
     :payment/pend pend
     :payment/paid-on paid-on)))


(s/def ::payment
  (s/keys :req [:db/id
                :payment/id
                :payment/amount
                :payment/status
                :payment/account]
          :opt [:payment/method
                :payment/due
                :payment/for
                :payment/pstart
                :payment/pend
                :payment/paid-on
                :stripe/source-id
                :stripe/invoice-id
                :stripe/charge-id]))
(s/fdef create
        :args (s/cat :amount float?
                     :account p/entity?
                     :opts (s/keys* :opt-un [::uuid
                                             ::due
                                             ::for
                                             ::status
                                             ::pstart
                                             ::pend
                                             ::charge-id
                                             ::invoice-id
                                             ::method
                                             ::paid-on
                                             ::source-id]))
        :ret ::payment)


(defn add-invoice
  "Add an invoice to this payment."
  [payment invoice-id]
  {:db/id             (td/id payment)
   :payment/method    :payment.method/stripe-invoice
   :stripe/invoice-id invoice-id})

(s/fdef add-invoice
        :args (s/cat :payment p/entity? :invoice-id string?)
        :ret map?)


(defn add-charge
  "Add a charge id to this payment."
  [payment charge-id]
  (let [m (when-not (invoice? payment) :payment.method/stripe-charge)]
    (toolbelt.core/assoc-when
     {:db/id            (td/id payment)
      :stripe/charge-id charge-id}
     :payment/method m)))

(s/fdef add-charge
        :args (s/cat :payment p/entity? :charge-id string?)
        :ret map?)


(defn add-check
  "Add a `check` to this `payment`."
  [payment check]
  (let [status (when-let [s (check/status check)] )]
    {:db/id          (td/id payment)
     :payment/check  (td/id check)
     :payment/method :payment.method/check}))

(s/fdef add-check
        :args (s/cat :payment p/entity? :check p/entity?)
        :ret map?)


(defn add-source
  "Add a `source-id` to this `payment`."
  [payment source-id]
  {:db/id            (td/id payment)
   :stripe/source-id source-id})

(s/fdef add-source
        :args (s/cat :payment p/entity? :source-id string?)
        :ret map?)


(defn is-paid
  "The payment is now paid."
  [payment]
  {:db/id          (td/id payment)
   :payment/status :payment.status/paid})

(s/fdef is-paid
        :args (s/cat :payment p/entity?)
        :ret map?)


(defn is-failed
  "The payment failed to go through."
  [payment]
  {:db/id          (td/id payment)
   :payment/status :payment.status/failed})

(s/fdef is-failed
        :args (s/cat :payment p/entity?)
        :ret map?)


(defn- default-due-date
  "The default due date is the fifth day of the same month as `start` date.
  Preserves the original year, month, hour, minute and second of `start` date."
  [start]
  (let [st (c/to-date-time start)]
    (c/to-date (t/date-time (t/year st)
                            (t/month st)
                            5
                            (t/hour st)
                            (t/minute st)
                            (t/second st)))))


(defn autopay
  "Create an autopay payment given the `member-license`."
  [member-license amount invoice-id period-start]
  (let [tz   (member-license/time-zone member-license)
        pend (date/end-of-month period-start tz)
        due  (date/end-of-day (default-due-date period-start) tz)]
    (create (float amount) (member-license/account member-license)
            :for :payment.for/rent
            :pstart (date/beginning-of-day period-start tz)
            :pend pend
            :paid-on period-start
            :invoice-id invoice-id
            :due due)))

(s/fdef autopay
        :args (s/cat :member-license p/entity?
                     :amount (s/and pos? number?)
                     :invoice-id string?
                     :period-start inst?)
        :ret ::payment)


;; =============================================================================
;; Queries
;; =============================================================================


(defn by-id
  "Look up a payment by its `uuid`."
  [db uuid]
  (d/entity db [:payment/id uuid]))

(s/fdef by-id
        :args (s/cat :db p/db? :uuid uuid?)
        :ret p/entityd?)


(defn by-charge-id
  "Look up a payment by its Stripe charge id. Payment must have a Stripe
  method."
  [db charge-id]
  (d/entity db [:stripe/charge-id charge-id]))

(s/fdef by-charge-id
        :args (s/cat :db p/db? :charge-id string?)
        :ret (s/or :entity p/entityd? :nothing nil?))


(defn by-invoice-id
  "Look up a payment by its Stripe invoice id. Payment must have a Stripe
  method."
  [db invoice-id]
  (d/entity db [:stripe/invoice-id invoice-id]))

(s/fdef by-invoice-id
        :args (s/cat :db p/db? :invoice-id string?)
        :ret (s/or :entity p/entityd? :nothing nil?))


(defn payments
  "All payments for `account`."
  [db account]
  (->> (d/q '[:find [?p ...]
              :in $ ?a
              :where
              [?p :payment/account ?a]]
            db (td/id account))
       (map (partial d/entity db))))

(s/fdef payments
        :args (s/cat :db p/db? :account p/entity?)
        :ret (s/* p/entityd?))
