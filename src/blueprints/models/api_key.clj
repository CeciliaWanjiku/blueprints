(ns blueprints.models.api-key
  (:refer-clojure :exclude [name])
  (:require [datomic.api :as d]
            [toolbelt.datomic :as td]
            [toolbelt.datomic.schema :as tds]
            [blueprints.models.account :as account]
            [clojure.spec.alpha :as s]
            [toolbelt.core :as tb]))


;; ==============================================================================
;; selectors ====================================================================
;; ==============================================================================


(defn id
  "The key itself."
  [api-key]
  (:api-key/id api-key))

(s/fdef id
        :args (s/cat :api-key td/entity?)
        :ret uuid?)


(defn account
  "The account entity that this key is for, if any."
  [api-key]
  (:api-key/account api-key))

(s/fdef account
        :args (s/cat :api-key td/entity?)
        :ret (s/nilable td/entityd?))


(defn name
  "The name used to identify the owner/application that this key belongs to."
  [api-key]
  (or (:api-key/name api-key) (account/short-name (account api-key))))

(s/fdef name
        :args (s/cat :api-key td/entity?)
        :ret string?)


(defn role
  "The role that this key acts with."
  [api-key]
  (or (:api-key/role api-key) (account/role (account api-key))))

(s/fdef role
        :args (s/cat :api-key td/entity?)
        :ret account/role?)


(defn issued
  "The instant in time at which this `api-key` was issued."
  [api-key]
  (:api-key/issued api-key))

(s/fdef issued
        :args (s/cat :api-key td/entity?)
        :ret inst?)


(defn active?
  "Is this `api-key` still active?"
  [api-key]
  (:api-key/active api-key true))

(s/fdef active?
        :args (s/cat :api-key td/entity?)
        :ret boolean?)


;; ==============================================================================
;; lookups ======================================================================
;; ==============================================================================


(defn by-id
  "Look up an API key by its id."
  [db key-id]
  (d/entity db [:api-key/id key-id]))

(s/fdef by-id
        :args (s/cat :db td/db? :key-id uuid?)
        :ret (s/nilable td/entityd?))


(defn by-account
  "Look up an API key by its owner."
  [db account]
  (let [a (td/entity account db)]
    (first (:api-key/_account a))))

(s/fdef by-account
        :args (s/cat :db td/db? :account td/entity?)
        :ret (s/nilable td/entityd?))


;; ==============================================================================
;; transactions =================================================================
;; ==============================================================================


(s/def ::id
  uuid?)

(s/def ::account
  td/entity?)

(s/def ::active
  boolean?)

(s/def ::name
  string?)

(s/def ::role
  account/role?)

(s/def ::create-opts
  (s/keys :opt-un [::id ::account ::active ::name ::role]))


(defn create
  "Create a new API key."
  [{:keys [id account name role active]
    :or   {id (d/squuid), active true}}]
  (assert (or (some? account) (some? role))
          "This key must be provided with either an `account` or a `role`.")
  (assert (or (some? account) (some? name))
          "This key must be provided with either an `account` or a `name`.")
  (tb/assoc-when
   {:db/id          (tds/tempid)
    :api-key/id     id
    :api-key/active active
    :api-key/issued (java.util.Date.)}
   :api-key/account (when-let [a account] (td/id a))
   :api-key/role role
   :api-key/name name))

(s/fdef create
        :args (s/cat :opts ::create-opts)
        :ret map?)


(defn deactivate
  "Deactivate this `api-key`."
  [api-key]
  {:db/id          (td/id api-key)
   :api-key/active false})

(s/fdef deactivate
        :args (s/cat :api-key td/entity?)
        :ret map?)
