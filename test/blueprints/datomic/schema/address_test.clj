(ns blueprints.datomic.schema.address-test
  (:require [blueprints.datomic]
            [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic.schema/conform-schema))


(deftest address-schema-conformed

  (test-attr a :address/lines
    (is (tdt/value-type a :string)))

  (test-attr a :address/region
    (is (tdt/value-type a :string)))

  (test-attr a :address/locality
    (is (tdt/value-type a :string)))

  (test-attr a :address/country
    (is (tdt/value-type a :string)))

  (test-attr a :address/postal-code
    (is (tdt/value-type a :string))))
