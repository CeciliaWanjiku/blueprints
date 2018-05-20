(ns blueprints.datomic.schema.member-license-test
  (:require [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr with-conn]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))


(deftest member-license-schema-conformed

  (test-attr a :member-license/license
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :member-license/rate
    (is (tdt/value-type a :float))
    (is (tdt/indexed a)))

  (test-attr a :member-license/starts
    (is (tdt/value-type a :instant))
    (is (tdt/indexed a)))

  (test-attr a :member-license/ends
    (is (tdt/value-type a :instant))
    (is (tdt/indexed a)))

  (test-attr a :member-license/unit
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :member-license/status
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :member-license/move-out
    (is (tdt/value-type a :boolean)))

  (test-attr _ :member-license.status/active)
  (test-attr _ :member-license.status/inactive)
  (test-attr _ :member-license.status/canceled)
  (test-attr _ :member-license.status/renewal))
