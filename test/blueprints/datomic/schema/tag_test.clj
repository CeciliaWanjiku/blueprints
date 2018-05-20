(ns blueprints.datomic.schema.tag-test
  (:require [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.test :refer :all]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))

(deftest tag-schema-conformed

  (test-attr a :tag/text
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :tag/category
    (is (tdt/value-type a :keyword))))
