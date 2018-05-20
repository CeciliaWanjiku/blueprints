(ns blueprints.schema.account-test
  (:require [toolbelt.datomic.test :as tdt :refer [test-attr]]
            [clojure.test :refer :all]
            [toolbelt.datomic :as td]))

(use-fixtures :once (tdt/conn-fixture blueprints.datomic/conform-schema))


(deftest account-schema-conformed

  (test-attr a :account/first-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :person/first-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))


  (test-attr a :account/middle-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :person/middle-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))


  (test-attr a :account/last-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :person/last-name
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))


  (test-attr a :account/phone-number
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :person/phone-number
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))


  (test-attr a :account/email
    (is (tdt/unique-identity a))
    (is (tdt/value-type a :string)))

  (test-attr a :account/password
    (is (tdt/value-type a :string))
    (is (not (tdt/indexed a))))

  (test-attr a :account/activated
    (is (tdt/value-type a :boolean))
    (is (not (tdt/indexed a))))

  (test-attr a :account/activation-hash
    (is (tdt/value-type a :string))
    (is (tdt/indexed a)))

  (test-attr a :account/role
    (is (tdt/value-type a :ref))
    (is (not (tdt/indexed a))))

  (test-attr _ :account.role/applicant)
  (test-attr _ :account.role/collaborator)
  (test-attr _ :account.role/onboarding)
  (test-attr _ :account.role/member)
  (test-attr _ :account.role/admin)

  (test-attr a :account/emergency-contact
    (is (tdt/value-type a :ref))
    (is (tdt/component a)))

  (test-attr a :account/licenses
    (is (tdt/value-type a :ref))
    (is (tdt/cardinality a :many))
    (is (tdt/component a))
    (is (tdt/indexed a)))

  (test-attr a :account/application
    (is (tdt/value-type a :ref))
    (is (tdt/indexed a)))

  (test-attr a :account/notes
    (is (tdt/value-type a :ref))
    (is (tdt/cardinality a :many))
    (is (not (tdt/indexed a))))

  (test-attr a :account/slack-handle
    (is (tdt/value-type a :string))
    (is (tdt/unique-identity a))))
