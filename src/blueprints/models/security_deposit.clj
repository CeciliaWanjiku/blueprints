(ns blueprints.models.security-deposit
  (:refer-clojure :exclude [type])
  (:require [blueprints.models.payment :as payment]
            [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]))


;; =============================================================================
;; Spec
;; =============================================================================


(s/def :deposit/method
  #{:deposit.method/ach
    :deposit.method/check})


(s/def :deposit/type
  #{:deposit.type/full
    :deposit.type/partial})


;; =============================================================================
;; Selectors
;; =============================================================================


(defn ^{:deprecated "1.10.0"} amount-received
  "The amount that we've received towards this deposit so far."
  [deposit]
  (get deposit :security-deposit/amount-received 0))

(s/fdef amount-received
        :args (s/cat :deposit td/entity?)
        :ret integer?)

(def ^{:deprecated "1.10.0"} received amount-received)


(defn ^{:deprecated "1.10.0"} amount-required
  "The amount required to consider this security deposit paid."
  [deposit]
  (get deposit :security-deposit/amount-required 0))

(s/fdef amount-required
        :args (s/cat :deposit td/entity?)
        :ret integer?)


(def ^{:deprecated "1.10.0"} required amount-required)


(defn ^{:added "1.10.0"} amount
  "The amount to be paid."
  [deposit]
  (:deposit/amount deposit))

(s/fdef amount
        :args (s/cat :deposit td/entity?)
        :ret float?)


(def due-by
  "The date that the security deposit is due on."
  :deposit/due)

(s/fdef due-by
        :args (s/cat :deposit td/entity?)
        :ret inst?)


(def due due-by)


(def account
  "The account that owns this deposit."
  :deposit/account)

(s/fdef account
        :args (s/cat :deposit td/entity?)
        :ret td/entity?)


(defn ^{:added "1.10.0"} payments
  [deposit]
  (:deposit/payments deposit))

(s/fdef payments
        :args (s/cat :deposit td/entity?)
        :ret (s/* td/entity?))


(def method
  "The payment method chosen during the onboarding flow."
  :deposit/method)

(s/fdef method
        :args (s/cat :deposit td/entity?)
        :ret :deposit/method)


(defn type
  "The deposit type chosen during the onboarding flow."
  [deposit]
  (:deposit/type deposit))

(s/fdef type
        :args (s/cat :deposit td/entity?)
        :ret :deposit/type)


(defn amount-remaining
  "The amount still remaining to be paid."
  [deposit]
  (let [paid (reduce
              #(+ %1 (if (or (payment/paid? %2) (payment/pending? %2))
                       (payment/amount %2)
                       0))
              0
              (payments deposit))]
    (- (amount deposit) paid)))

(s/fdef amount-remaining
        :args (s/cat :deposit td/entity?)
        :ret integer?)


(defn amount-paid
  "The amount that has been paid."
  [deposit]
  (reduce
   #(+ %1 (if (payment/paid? %2) (payment/amount %2) 0))
   0
   (payments deposit)))

(s/fdef amount-paid
        :args (s/cat :deposit td/entity?)
        :ret integer?)


(defn amount-pending
  "The amount that is still pending, either in the form of charges or checks."
  [deposit]
  (reduce
   #(+ %1 (if (payment/pending? %2) (payment/amount %2) 0))
   0
   (payments deposit)))

(s/fdef amount-pending
        :args (s/cat :deposit td/entity?)
        :ret integer?)


(defn refund-status
  "The status of the refund. `nil` when not refunded, initiated, successful or
  failed."
  [deposit]
  (:deposit/refund-status deposit))

(s/fdef refund-status
        :args (s/cat :deposit td/entity?)
        :ret (s/or :nothing nil?
                   :status #{:deposit.refund-status/initiated
                             :deposit.refund-status/successful
                             :deposit.refund-status/failed}))


(defn line-items
  "The line-items of the deposit."
  [deposit]
  (:deposit/lines deposit))

(s/fdef line-items
        :args (s/cat :deposit td/entity?)
        :ret (s/* td/entityd?))


(defn line-items-by-subtype
  [deposit subtype]
  (filterv
   (comp #(some #{subtype} %) :line-item/subtypes)
   (line-items deposit)))


(defn- sum-amount [items]
  (reduce
   (fn [sum item]
     (cond
       (number? (:price item))
       (+ sum (:price item))

       :else
       sum))
   0
   items))


(defn refund-amount
  "Total deposit refund amount"
  [deposit]
  (let [deposit-amount (or (amount deposit) 0)
        charges        (line-items-by-subtype deposit :refund-charge)
        credits        (line-items-by-subtype deposit :refund-credit)
        charge-amount  (sum-amount charges)
        credit-amount  (sum-amount credits)
        refund-amount  (-> deposit-amount
                           (- charge-amount)
                           (+ credit-amount))]
    (if (number? refund-amount)
      refund-amount
      0)))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn is-unpaid?
  "A deposit is considered /unpaid/ if we have received no payment towards
  it (pending payments excepted)."
  [deposit]
  (and (= (amount-remaining deposit) (amount deposit))
       (not= 0.0 (amount-remaining deposit))))

(s/fdef is-unpaid?
        :args (s/cat :deposit td/entity?)
        :ret boolean?)


(def is-paid?
  "Has the deposit been paid in any capacity? Because we allow partial payments,
  this will return true if /any/ amount of payment has been made."
  (comp not is-unpaid?))


(defn paid-in-full?
  "Is the deposit completely paid?"
  [deposit]
  (<= (amount-remaining deposit) 0))

(s/fdef paid-in-full?
        :args (s/cat :deposit td/entity?)
        :ret boolean?)


(defn partially-paid?
  "Is this deposit partially paid and NOT fully paid?"
  [deposit]
  (and (not (paid-in-full? deposit))
       (is-paid? deposit)))

(s/fdef partially-paid?
        :args (s/cat :deposit td/entity?)
        :ret boolean?)


(defn is-refundable?
  "Can this security deposit be refunded via Stripe?"
  [deposit]
  (and (nil? (refund-status deposit))
       (seq (payments deposit))
       (let [charge-total (->> (payments deposit)
                               (filter #(and (payment/charge? %1) (payment/paid? %1)))
                               (reduce #(+ %1 (payment/amount %2)) 0))]
         (= (amount deposit) charge-total))))

(s/fdef is-refundable?
        :args (s/cat :deposit td/entity?)
        :ret boolean?)


;; =============================================================================
;; Transactions
;; =============================================================================


(defn ^{:added "1.10.0"} add-payment
  "Add a payment to this deposit."
  [deposit payment]
  {:db/id            (td/id deposit)
   :deposit/payments (td/id payment)})

(s/fdef add-payment
        :args (s/cat :deposit td/entity? :payment td/entity?)
        :ret (s/keys :req [:db/id :deposit/payments]))


(defn create
  "Produce transaction data to create a security deposit entity for `account`.

  Only requires an `amount` (and `account` of course), since other details are
  filled in by `account` during the onboarding flow."
  [account amount]
  {:db/id           (d/tempid :db.part/starcity)
   :deposit/account (td/id account)
   :deposit/amount  (float amount)})

(s/fdef create
        :args (s/cat :account td/entity?
                     :amount number?)
        :ret (s/keys :req [:db/id :deposit/account :deposit/amount]))


;; =============================================================================
;; Lookups
;; =============================================================================


(def by-account
  "Retrieve `security-deposit` given the owning `account`."
  (comp first :deposit/_account))

(s/fdef by-account
        :args (s/cat :account td/entityd?)
        :ret td/entityd?)


(defn by-payment
  "Produce the security deposit given `payment`."
  [payment]
  (:deposit/_payments payment))

(s/fdef by-payment
        :args (s/cat :payment td/entityd?)
        :ret (s/or :entity td/entityd? :nothing nil?))


(defn by-charge-id
  "Look up a security deposit given a Stripe charge id."
  [db charge-id]
  (->> (d/q '[:find ?e .
              :in $ ?c
              :where
              [?e :deposit/payments ?p]
              [?p :stripe/charge-id ?c]]
            db charge-id)
       (d/entity db)))

(s/fdef by-charge-id
        :args (s/cat :db td/db? :charge-id string?)
        :ret (s/or :entity td/entity? :nothing nil?))
