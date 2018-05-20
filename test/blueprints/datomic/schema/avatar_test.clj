(ns blueprints.datomic.schema.avatar-test
  (:require [blueprints.datomic]
            [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))


(deftest avatar-schema-conformed

  (test-attr a :avatar/name
    (is (tdt/value-type a :keyword))
    (is (tdt/unique-identity a)))

  (test-attr a :avatar/url
    (is (tdt/value-type a :string))
    (is (not (tdt/indexed a))))

  (test-attr a :avatar/account
    (is (tdt/value-type a :ref))
    (is (not (tdt/indexed a)))))
