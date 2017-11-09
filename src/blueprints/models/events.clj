(ns blueprints.models.events
  (:require [blueprints.models.account :as account]
            [blueprints.models.event :as event]
            [blueprints.models.note :as note]
            [blueprints.models.payment :as payment]
            [clojure.spec :as s]
            [toolbelt.core :as tb]
            [clojure.string :as string]
            [toolbelt.datomic :as td]
            [toolbelt.predicates :as p]))


;; =============================================================================
;; Accounts
;; =============================================================================


(defn reset-password
  "Reset `account`'s password."
  [account]
  (event/job :account/reset-password {:params {:account-id (td/id account)}}))

(s/fdef reset-password
        :args (s/cat :account p/entity?)
        :ret map?)


(defn create-account
  "Create a new account."
  [email password first-name last-name & {:keys [middle-name]}]
  (event/job :account/create {:params (tb/assoc-when
                                       {:email      email
                                        :password   password
                                        :first-name first-name
                                        :last-name  last-name}
                                       :middle-name middle-name)}))

(s/fdef create-account
        :args (s/cat :email string?
                     :password string?
                     :first-name string?
                     :last-name string?
                     :opts (s/keys* :opt-un [::middle-name]))
        :ret map?)


(defn create-collaborator
  "Create a new collaborator."
  [email type message]
  (event/job :collaborator/create {:params {:email   email
                                            :type    type
                                            :message message}}))

(s/fdef create-collaborator
        :args (s/cat :email string?
                     :type #{"real-estate" "community-stakeholder" "vendor" "investor"}
                     :message string?)
        :ret map?)


(defn account-promoted
  "`account` has been promoted to membership."
  [account]
  (event/job :account/promoted {:params {:account-id (td/id account)}}))

(s/fdef account-promoted
        :args (s/cat :account p/entity?)
        :ret map?)


(defn account-approved
  "`account` has been approved for membership."
  [account]
  (event/job :account/approved {:params {:account-id (td/id account)}}))

(s/fdef account-approved
        :args (s/cat :account p/entity?)
        :ret map?)


;; =============================================================================
;; Deposit
;; =============================================================================


(defn deposit-payment-made
  [account charge-id]
  (event/job :deposit/payment-made {:params {:account-id (td/id account)
                                             :charge     charge-id}}))

(s/fdef deposit-payment-made
        :args (s/cat :account p/entity? :charge-id string?)
        :ret map?)


(defn remainder-deposit-payment-made
  [account charge-id]
  (event/job :deposit.remainder/payment-made {:params {:account-id (td/id account)
                                                       :charge     charge-id}}))

(s/fdef remainder-deposit-payment-made
        :args (s/cat :account p/entity? :charge-id string?)
        :ret map?)


(defn initiate-refund
  "Initiate the security deposit refund procedure."
  [deposit amount reasons]
  (event/job :deposit/refund {:params (tb/assoc-when
                                       {:deposit-id (td/id deposit)
                                        :amount     amount}
                                       :reasons reasons)}))

(s/fdef initiate-refund
        :args (s/cat :deposit p/entity?
                     :amount number?
                     :reasons (s/or :nothing nil? :string string?))
        :ret map?)


(defn alert-unpaid-deposits
  "Send alerts to indicate that `deposits` are unpaid as of as-of time `t`."
  [deposits t]
  (event/job :deposits/alert-unpaid
             {:params {:deposit-ids (map td/id deposits)
                       :as-of       t}}))

(s/fdef alert-unpaid-deposits
        :args (s/cat :deposits (s/+ p/entity?) :t inst?)
        :ret map?)


(defn alert-deposit-due
  "Send alerts to indicate that `deposit` is due soon relative to time `t`."
  [deposit t]
  (event/notify :deposit/due {:params {:deposit-id (td/id deposit)
                                       :as-of      t}}))

(s/fdef alert-deposit-due
        :args (s/cat :deposit p/entity? :t inst?)
        :ret map?)


;; =============================================================================
;; Newsletter
;; =============================================================================


(defn add-newsletter-subscriber
  "Add `email` address to our newsletter."
  [email]
  (event/job :newsletter/subscribe {:params {:email email}}))

(s/fdef add-newsletter-subscriber
        :args (s/cat :email string?)
        :ret map?)


;; =============================================================================
;; Notes
;; =============================================================================


(defn note-created
  "A `note` was created."
  [note]
  (event/job :note/created {:params {:uuid (note/uuid note)}}))

(s/fdef note-created
        :args (s/cat :note p/entity?)
        :ret map?)


(defn note-comment-created
  "A `comment` has been added to a `note`."
  [note comment]
  (event/job :note.comment/created {:params {:comment-uuid (note/uuid comment)
                                             :note-id      (td/id note)}}))

(s/fdef added-note-comment
        :args (s/cat :note p/entity? :comment p/entity?)
        :ret map?)


;; =============================================================================
;; Orders
;; =============================================================================


(defn process-order
  "Indicate that `account` has submitted `order` to be processed."
  [account order]
  (event/job :order/process {:params {:order-id   (td/id order)
                                      :account-id (td/id account)}}))

(s/fdef process-order
        :args (s/cat :account p/entity?
                     :order p/entity?)
        :ret map?)


(defn order-placed
  "Indicates that `order` has been placed by `account`. Can optionally specify
  whether or not to notify the order owner."
  [account order & [notify]]
  (event/job :order/placed {:params {:order-id   (td/id order)
                                     :account-id (td/id account)
                                     :notify     (boolean notify)}}))

(s/fdef order-placed
        :args (s/cat :account p/entity?
                     :order p/entity?
                     :notify (s/? boolean?))
        :ret map?)


(defn order-fulfilled
  "Indicates that `order` has been fulfilled by `account`. Can optionally specify
  whether or not to notify the order owner."
  [account order & [notify]]
  (event/job :order/fulfilled {:params {:order-id   (td/id order)
                                        :account-id (td/id account)
                                        :notify     (boolean notify)}}))

(s/fdef order-fulfilled
        :args (s/cat :account p/entity?
                     :order p/entity?
                     :notify (s/? boolean?))
        :ret map?)


(defn order-canceled
  "Indicates that `order` has been canceled by `account`. Can optionally specify
  whether or not to notify the order owner."
  [account order & [notify]]
  (event/job :order/canceled {:params {:order-id   (td/id order)
                                       :account-id (td/id account)
                                       :notify     (boolean notify)}}))

(s/fdef order-canceled
        :args (s/cat :account p/entity?
                     :order p/entity?
                     :notify (s/? boolean?))
        :ret map?)


;; =============================================================================
;; Payments
;; =============================================================================


(defn alert-payment-due
  "Send alerts to indicate that `payment` is due soon relative to time `t`."
  [payment t]
  (event/notify :payment/due {:params {:payment-id (td/id payment)
                                       :as-of      t}}))

(s/fdef alert-payment-due
        :args (s/cat :payment p/entity? :t inst?)
        :ret map?)



;; =============================================================================
;; Rent
;; =============================================================================


(defn create-monthly-rent-payments
  "Create rent payments for the current time `period` for members that are not
  on autopay."
  [period]
  (event/job :rent-payments/create-all {:params {:period period}}))

(s/fdef create-monthly-rent-payments
        :args (s/cat :period inst?)
        :ret map?)


(defn rent-payment-made
  "A member has paid his/her rent by ACH."
  [account payment]
  (event/job :rent-payment.payment/ach {:params {:account-id (td/id account)
                                                 :payment-id (td/id payment)}}))

(s/fdef rent-payment-made
        :args (s/cat :account p/entity? :payment p/entity?)
        :ret map?)


(defn alert-all-unpaid-rent
  "Send alerts to indicate that rent `payments` are unpaid as of as-of time `t`."
  [payments t]
  (event/job :rent-payments/alert-unpaid
             {:params {:payment-ids (map td/id payments)
                       :as-of       t}}))

(s/fdef alert-all-unpaid-rent
        :args (s/cat :payments (s/+ p/entity?) :date inst?)
        :ret map?)


;; =============================================================================
;; Scheduler
;; =============================================================================


(defn process-daily-tasks
  "Event that triggers daily tasks."
  [t]
  (event/job :scheduler/daily {:params {:as-of t}}))

(s/fdef process-daily-tasks
        :args (s/cat :t inst?)
        :ret map?)


;; =============================================================================
;; Session
;; =============================================================================


(defn revoke-session
  "Revoke `account`'s session."
  [account]
  (event/job :session/revoke {:params {:account-id (td/id account)}}))

(s/fdef revoke-session
        :args (s/cat :account p/entity?)
        :ret map?)


;; =============================================================================
;; Stripe
;; =============================================================================


(defn- snake->kebab [s]
  (string/replace s #"_" "-"))


(defn- event-type->key [et]
  (let [parts (->> (concat '("stripe" "event") (string/split et #"\."))
                   (map snake->kebab))]
    (keyword
     (string/join "." (butlast parts))
     (last parts))))

(s/fdef event-type->key
        :args (s/cat :type string?)
        :ret keyword?)


(defn stripe-event
  [event-id event-type connect-id]
  (let [meta (when-some [x connect-id] {:managed-account x})]
    (event/stripe (event-type->key event-type)
                  (tb/assoc-when {:id event-id} :meta meta))))

(s/fdef stripe-event
        :args (s/cat :event-id string?
                     :event-type string?
                     :connect-id (s/or :string string? :nothing nil?))
        :ret map?)


(defn delete-source
  "Delete `customer`'s source."
  [customer source-id & [triggered-by]]
  (event/job :stripe.customer.source/delete
             (tb/assoc-when
              {:params {:customer  customer
                        :source-id source-id}}
              :triggered-by triggered-by)))

(s/fdef delete-source
        :args (s/cat :customer string?
                     :source-id string?
                     :triggered-by (s/? p/entity?))
        :ret map?)
