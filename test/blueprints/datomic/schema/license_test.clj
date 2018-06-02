(ns blueprints.datomic.schema.license-test
  (:require [blueprints.datomic]
            [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic.schema/conform-schema))


(deftest license-schema-conformed

  (test-attr a :license/term
    (is (tdt/value-type a :long))
    (is (tdt/indexed a)))

  (test-attr a :license/available
    (is (tdt/value-type a :boolean))
    (is (tdt/indexed a))))
