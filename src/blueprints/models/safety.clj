(ns blueprints.models.safety
  (:require [datomic.api :as d]
            [toolbelt.datomic :as td]))


;; ==============================================================================
;; transactions =================================================================
;; ==============================================================================


(defn create [account]
  {:db/id                    (d/tempid :db.part/starcity)
   :community-safety/account (td/id account)})


;; ==============================================================================
;; queries ======================================================================
;; ==============================================================================


(defn by-account
  "Retrieve a community-safeties by account."
  [db account]
  (->> (d/q '[:find ?e .
              :in $ ?a
              :where
              [?e :community-safety/account ?a]]
            db (:db/id account))
       (d/entity db)))
