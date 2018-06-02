(ns blueprints.datomic.schema.referral-test
  (:require [blueprints.datomic]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.test :refer :all]
            [clojure.string :as string]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic.schema/conform-schema))


(deftest referral-schema-conformed

  (test-attr a :referral/source
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :referral/account
    (is (tdt/value-type a :ref)))

  (test-attr a :referral/from
    (is (tdt/value-type a :ref)))

  (test-attr a :referral/tour-for
    (is (tdt/value-type a :ref))))
