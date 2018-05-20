(ns blueprints.datomic.schema.application-test
  (:require [clojure.test :refer :all]
            [toolbelt.datomic.test :as tdt :refer [test-attr]]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))


(deftest application-schema-conformed

  (test-attr a :application/communities
    (is (tdt/value-type a :ref))
    (is (tdt/cardinality a :many))
    (is (tdt/indexed a)))

  (test-attr a :application/license
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :application/move-in
    (is (tdt/value-type a :instant))
    (is (tdt/indexed a)))

  (test-attr a :application/pet
    (is (tdt/value-type a :ref))
    (is (not (tdt/indexed a)))
    (is (tdt/component a)))

  (test-attr a :application/has-pet
    (is (tdt/value-type a :boolean))
    (is (not (tdt/indexed a))))

  (test-attr a :application/fitness
    (is (tdt/value-type a :ref))
    (is (tdt/component a))
    (is (not (tdt/indexed a))))

  (test-attr a :application/address
    (is (tdt/value-type a :ref))
    (is (tdt/component a)))

  (test-attr a :application/status
    (is (tdt/value-type a :ref))
    (is (not (tdt/indexed a))))

  (test-attr _ :application.status/in-progress)
  (test-attr _ :application.status/submitted)
  (test-attr _ :application.status/approved)
  (test-attr _ :application.status/rejected))


(deftest fitness-schema-conformed

  (test-attr a :fitness/experience
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :fitness/skills
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :fitness/free-time
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :fitness/interested
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :fitness/dealbreakers
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a)))

  (test-attr a :fitness/conflicts
    (is (tdt/value-type a :string))
    (is (tdt/fulltext a))))


(deftest pet-schema-conformed

  (test-attr a :pet/type
    (is (tdt/value-type a :keyword)))

  (test-attr a :pet/breed
    (is (tdt/value-type a :string)))

  (test-attr a :pet/weight
    (is (tdt/value-type a :long)))

  (test-attr a :pet/sterile
    (is (tdt/value-type a :boolean)))

  (test-attr a :pet/vaccines
    (is (tdt/value-type a :boolean)))

  (test-attr a :pet/bitten
    (is (tdt/value-type a :boolean)))

  (test-attr a :pet/demeanor
    (is (tdt/value-type a :string)))

  (test-attr a :pet/daytime-care
    (is (tdt/value-type a :string))))


(deftest community-safety-schema-conformed

  (test-attr a :community-safety/account
    (is (tdt/value-type a :ref)))

  (test-attr a :community-safety/report-url
    (is (tdt/value-type a :string)))

  (test-attr a :community-safety/wants-report?
    (is (tdt/value-type a :boolean)))

  (test-attr a :community-safety/consent-given?
    (is (tdt/value-type a :boolean))))
