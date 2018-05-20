(ns user
  (:require [blueprints.core :as blueprints]
            [clojure.spec.test.alpha :as stest]
            [clojure.tools.namespace.repl :refer [refresh set-refresh-dirs]]
            [datomic.api :as d]
            [mount.core :as mount :refer [defstate]]))

(def start mount/start)


(def stop mount/stop)


(defn go []
  (start)
  (stest/instrument)
  :ready)


(defn reset []
  (stop)
  (refresh :after 'user/go))
