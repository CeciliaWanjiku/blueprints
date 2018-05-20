(ns blueprints.datomic.schema.license-test
  (:require [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr with-conn]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))


(deftest license-schema-conformed

  (test-attr a :license/term
    (is (tdt/value-type a :long))
    (is (tdt/indexed a)))

  (test-attr a :license/available
    (is (tdt/value-type a :boolean))
    (is (tdt/indexed a))))
