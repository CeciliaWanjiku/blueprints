(ns user
  (:require [blueprints.core]
            [blueprints.config :as config :refer [config]]
            [blueprints.datomic :refer [conn]]
            [blueprints.teller :refer [teller]]
            [blueprints.seed :as seed]
            [clojure.spec.test.alpha :as stest]
            [clojure.tools.namespace.repl :refer [refresh set-refresh-dirs]]
            [datomic.api :as d]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :as timbre]))

;; ==============================================================================
;; reloaded =====================================================================
;; ==============================================================================


(def start mount/start)


(def stop mount/stop)


(defn go []
  (start)
  (stest/instrument)
  :ready)


(defn reset []
  (stop)
  (refresh :after 'user/go))


;; ==============================================================================
;; dev state ====================================================================
;; ==============================================================================


(defn- in-memory-db?
  "There's a more robust way to do this, but it's not really necessary ATM."
  [uri]
  (clojure.string/starts-with? uri "datomic:mem"))


(defstate seeder
  :start (when (in-memory-db? (config/datomic-uri config))
           (timbre/debug "seeding dev database...")
           (seed/seed conn)
           (seed/seed-teller teller)))
