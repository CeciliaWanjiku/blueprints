(ns blueprints.models.approval
  (:require [blueprints.models.application :as application]
            [blueprints.models.onboard :as onboard]
            [blueprints.models.property :as property]
            [blueprints.models.security-deposit :as deposit]
            [blueprints.models.unit :as unit]
            [clojure.spec.alpha :as s]
            [datomic.api :as d]
            [toolbelt.date :as date]
            [toolbelt.datomic :as td]))

;; =============================================================================
;; Selectors
;; =============================================================================


(def approver
  "The admin `account` that did the approving."
  :approval/approver)

(s/fdef approver
        :args (s/cat :approval td/entity?)
        :ret td/entity?)


(def move-in
  "The move-in date."
  :approval/move-in)

(s/fdef move-in
        :args (s/cat :approval td/entity?)
        :ret inst?)


(def unit
  "The `unit` that `account` is approved to live in."
  :approval/unit)

(s/fdef unit
        :args (s/cat :approval td/entity?)
        :ret td/entity?)


(def license
  "The `license` (term) that `account` was approved for."
  :approval/license)

(s/fdef license
        :args (s/cat :approval td/entity?)
        :ret td/entity?)


(def property
  "The property that approval is for."
  (comp unit/property unit))

(s/fdef property
        :args (s/cat :approval td/entity?)
        :ret td/entity?)


;; =============================================================================
;; Transactions
;; =============================================================================


(defn create
  "Produce transaction data required to create an approval entity."
  [approver approvee unit license move-in]
  (let [tz (-> unit unit/property property/time-zone)]
    {:db/id             (d/tempid :db.part/starcity)
     :approval/account  (:db/id approvee)
     :approval/approver (:db/id approver)
     :approval/unit     (:db/id unit)
     :approval/license  (:db/id license)
     :approval/move-in  (date/beginning-of-day move-in tz) ; ensure beginning of day
     :approval/status   :approval.status/pending}))

(s/fdef create
        :args (s/cat :approver td/entity?
                     :approvee td/entity?
                     :unit td/entity?
                     :license td/entity?
                     :move-in inst?)
        :ret (s/keys :req [:db/id
                           :approval/account
                           :approval/approver
                           :approval/unit
                           :approval/license
                           :approval/move-in
                           :approval/status]))


(defn approve
  "Approve `approvee` by creating an `approval` entity and flipping the
  necessary bits elswhere in the database.

  More specifically, this means:
  - Change `account`'s role to onboarding
  - Create a security deposit stub
  - Mark the application as approved"
  [approver approvee unit license move-in]
  (let [tz      (-> unit unit/property property/time-zone)
        move-in (date/beginning-of-day move-in tz)]
    [(create approver approvee unit license move-in)
     ;; Change role
     {:db/id (:db/id approvee) :account/role :account.role/onboarding}
     (deposit/create approvee (unit/rate unit license))
     (onboard/create approvee)
     (application/change-status (:account/application approvee)
                                :application.status/approved)]))

(s/fdef approve
        :args (s/cat :approver td/entity?
                     :approvee td/entity?
                     :unit td/entity?
                     :license td/entity?
                     :move-in inst?)
        :ret (s/and vector? (s/+ map?)))


;; =============================================================================
;; Queries
;; =============================================================================


(def by-account
  "Look up the `approval` entity by `account`."
  (comp first :approval/_account))

(s/fdef by-account
        :args (s/cat :account td/entity?)
        :ret td/entity?)
