(ns blueprints.models.customer
  (:require [clojure.spec :as s]
            [toolbelt.predicates :as p]
            [plumbing.core :as plumbing]
            [datomic.api :as d]))

;; =============================================================================
;; Selectors
;; =============================================================================

(def id
  "The id of the Stripe customer."
  :stripe-customer/customer-id)

(s/fdef id
        :args (s/cat :customer p/entity?)
        :ret string?)

(def account
  "The account that this customer belongs to."
  :stripe-customer/account)

(s/fdef account
        :args (s/cat :customer p/entity?)
        :ret p/entity?)

(def bank-token
  "The customer's bank account token, if any."
  :stripe-customer/bank-account-token)

(s/fdef bank-token
        :args (s/cat :customer p/entity?)
        :ret (s/? string?))

(def managing-property
  "The property that this Stripe customer belongs to, if any."
  :stripe-customer/managed)

(s/fdef managing-property
        :args (s/cat :customer p/entity?)
        :ret (s/? p/entity?))


;; =============================================================================
;; Transactions
;; =============================================================================

(defn create
  "Create a new Stripe customer."
  [customer-id account & {:keys [bank-account-token managing-property]}]
  (plumbing/assoc-when
   {:db/id                       (d/tempid :db.part/starcity)
    :stripe-customer/customer-id customer-id
    :stripe-customer/account     (:db/id account)}
   :stripe-customer/bank-account-token bank-account-token
   :stripe-customer/managed (:db/id managing-property)))

(s/def ::bank-account-token string?)
(s/def ::managing-property p/entity?)
(s/fdef create
        :args (s/cat :customer-id string?
                     :account p/entity?
                     :opts (s/keys* :opt-un [::bank-account-token
                                             ::managing-property])))

;; =============================================================================
;; Queries
;; =============================================================================

(defn by-account
  "Retrieve the `stripe-customer` that belongs to this account. Produces the
  customer that is on the Stripe master account, NOT the managed one -- the
  customer on the managed account will be used *only* for autopay."
  [db account]
  (->> (d/q '[:find ?e .
              :in $ ?a
              :where
              [?e :stripe-customer/account ?a]
              [(missing? $ ?e :stripe-customer/managed)]]
            db (:db/id account))
       (d/entity db)))

(s/fdef by-account
        :args (s/cat :db p/db? :account p/entity?)
        :ret (s/? p/entity?))
