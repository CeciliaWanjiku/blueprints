(ns blueprints.datomic.schema.note-test
  (:require [blueprints.datomic]
            [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))


(deftest note-schema-conformed

  (test-attr a :note/uuid
    (is (tdt/value-type a :uuid))
    (is (tdt/unique-identity a)))

  (test-attr a :note/author
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :note/subject
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :note/content
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :note/children
    (is (tdt/value-type a :ref))
    (is (tdt/cardinality a :many))
    (is (tdt/indexed a))
    (is (tdt/component a)))

  (test-attr a :note/tags
    (is (tdt/value-type a :ref))
    (is (tdt/cardinality a :many))
    (is (tdt/indexed a)))

  (test-attr a :note/ref
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :ticket/status
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :ticket/assigned-to
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a))))
