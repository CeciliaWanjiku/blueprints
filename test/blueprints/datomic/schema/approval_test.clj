(ns blueprints.datomic.schema.approval-test
  (:require [blueprints.datomic]
            [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))


(deftest approval-schema-conformed

  (test-attr a :approval/account
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :approval/approver
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :approval/unit
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :approval/move-in
    (is (tdt/value-type a :instant))
    (is (tdt/indexed a)))

  (test-attr a :approval/license
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :approval/status
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr _ :approval.status/pending)
  (test-attr _ :approval.status/canceled)
  (test-attr _ :approval.status/approved))
