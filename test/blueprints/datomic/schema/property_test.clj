(ns blueprints.datomic.schema.property-test
  (:require [blueprints.datomic]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.test :refer :all]
            [clojure.string :as string]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))


(deftest community-schema-conformed

  (test-attr a :community/name
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :community/description
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :community/code
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a))
    (is (tdt/unique-identity a)))

  (test-attr a :community/address
    (is (tdt/value-type a :ref))
    (is (tdt/component a)))

  (test-attr a :community/units
    (is (tdt/value-type a :ref))
    (is (tdt/cardinality a :many)))

  (test-attr a :community/available-on
    (is (tdt/value-type a :instant))
    (is (tdt/indexed a)))

  (test-attr a :community/tours
    (is (tdt/value-type a :boolean))
    (is (tdt/indexed a)))

  (test-attr a :community/cover-image-url
    (is (tdt/value-type a :string))
    (is (string/starts-with? (:db/doc a) "DEPRECATED"))))


(deftest property-schema-conformed

  (test-attr a :property/name
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :property/description
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :property/code
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a))
    (is (tdt/unique-identity a)))

  (test-attr a :property/address
    (is (tdt/value-type a :ref))
    (is (tdt/component a)))

  (test-attr a :property/units
    (is (tdt/value-type a :ref))
    (is (tdt/cardinality a :many)))

  (test-attr a :property/available-on
    (is (tdt/value-type a :instant))
    (is (tdt/indexed a)))

  (test-attr a :property/ops-fee-rent
    (is (tdt/value-type a :float))
    (is (tdt/indexed a)))

  (test-attr a :property/ops-fee-orders
    (is (tdt/value-type a :float))
    (is (tdt/indexed a)))

  (test-attr a :property/tours
    (is (tdt/value-type a :boolean))
    (is (tdt/indexed a)))

  (test-attr a :property/rent-connect-id
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :property/deposit-connect-id
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :property/cover-image-url
    (is (tdt/value-type a :string))
    (is (string/starts-with? (:db/doc a) "DEPRECATED"))))
