(ns blueprints.schema.datomic.event-test
  (:require [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr with-conn]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))


(deftest schema-conformed?
  (test-attr a :event/uuid
    (is (tdt/value-type a :uuid))
    (is (tdt/unique-identity a)))

  (test-attr a :event/id
    (is (tdt/value-type a :string))
    (is (tdt/unique-identity a)))

  (test-attr a :event/key
    (is (tdt/value-type a :keyword))
    (is (tdt/indexed a)))

  (test-attr a :event/topic
    (is (tdt/value-type a :keyword))
    (is (tdt/indexed a)))

  (test-attr a :event/params
    (is (tdt/value-type a :string)))

  (test-attr a :event/meta
    (is (tdt/value-type a :string)))

  (test-attr a :event/status
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :event/triggered-by
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr _ :event.status/pending)
  (test-attr _ :event.status/successful)
  (test-attr _ :event.status/failed))
