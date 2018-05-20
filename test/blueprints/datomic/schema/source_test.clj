(ns blueprints.datomic.schema.source-test
  (:require [blueprints.datomic]
            [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))


(deftest source-schema-conformed

  (test-attr a :source/account
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a))))
