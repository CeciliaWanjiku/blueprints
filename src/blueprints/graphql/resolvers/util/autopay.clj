(ns blueprints.graphql.resolvers.util.autopay
  (:require [blueprints.models.account :as account]
            [blueprints.models.member-license :as member-license]
            [blueprints.models.unit :as unit]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [teller.customer :as tcustomer]
            [teller.property :as tproperty]
            [toolbelt.date :as date]))

(defn autopay-start
  "The date that autopay should start for this `customer`."
  [customer]
  (let [property (tcustomer/property customer)
        tz       (t/time-zone-for-id (tproperty/timezone property))]
    (-> (c/to-date (t/plus (t/now) (t/months 1)))
        (date/beginning-of-month tz))))


(defn plan-name
  [teller license]
  (let [account       (member-license/account license)
        email         (account/email account)
        unit-name     (unit/code (member-license/unit license))
        customer      (tcustomer/by-account teller account)
        property      (tcustomer/property customer)
        property-name (tproperty/name property)]
    (str "autopay for " email " @ " property-name " in " unit-name)))
