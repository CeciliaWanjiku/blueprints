(ns blueprints.schema.onboard-test
  (:require [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer :all]))

(use-fixtures :once (tdt/conn-fixture blueprints.schema/conform))


(deftest onboard-conformed?
  (test-attr a :onboard/account
    (is (value-type a :ref))
    (is (indexed a)))

  (test-attr a :onboard/move-in
    (is (value-type a :instant))
    (is (indexed a)))

  (test-attr a :onboard/seen
    (is (value-type a :keyword))
    (is (cardinality a :many))
    (is (indexed a))))
