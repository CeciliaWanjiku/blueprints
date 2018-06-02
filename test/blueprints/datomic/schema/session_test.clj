(ns blueprints.datomic.schema.session-test
  (:require [blueprints.datomic]
            [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic.schema/conform-schema))


(deftest session-test-conform

  (test-attr a :session/key
    (is (tdt/value-type a :string))
    (is (tdt/unique-identity a)))

  (test-attr a :session/account
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :session/value
    (is (tdt/value-type a :bytes))))
