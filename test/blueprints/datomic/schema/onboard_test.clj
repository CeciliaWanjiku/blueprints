(ns blueprints.datomic.schema.onboard-test
  (:require [blueprints.datomic]
            [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic.schema/conform-schema))


(deftest onboard-conformed?
  (test-attr a :onboard/account
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :onboard/move-in
    (is (tdt/value-type a :instant))
    (is (tdt/indexed a)))

  (test-attr a :onboard/seen
    (is (tdt/value-type a :keyword))
    (is (tdt/cardinality a :many))
    (is (tdt/indexed a))))
