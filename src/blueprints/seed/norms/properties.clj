(ns blueprints.seed.norms.properties
  (:require [datomic.api :as d]
            [toolbelt.core :as tb]))

;; =============================================================================
;; Property TX Generation Helpers
;; =============================================================================

(defn license [conn term]
  (d/q '[:find ?e .
         :in $ ?term
         :where [?e :license/term ?term]]
       (d/db conn) term))

(defn property-licenses [conn & ls]
  (map
   (fn [[term price]]
     {:property-license/license    (license conn term)
      :property-license/base-price price})
   ls))

(defn address [lines]
  {:address/lines lines
   :address/city  "San Francisco"})

(defn units [property-name n]
  (for [i (range n)]
    {:unit/name (format "%s-%s" property-name (inc i))}))

(defn property
  [part name internal-name available-on address licenses units
   & {:keys [managed-account-id ops-fee tours slack-channel tipe-document-id cover-image-url]
      :or   {tours false}}]
  (tb/assoc-when
   {:db/id                  (d/tempid part)
    :property/name          name
    :property/internal-name internal-name
    :property/available-on  available-on
    :property/licenses      licenses
    :property/units         units
    :property/tours         tours}
   :slack/channel slack-channel
   :property/managed-account-id managed-account-id
   :property/ops-fee ops-fee
   :tipe/document-id tipe-document-id
   :property/cover-image-url cover-image-url))

;; =============================================================================
;; Meat
;; =============================================================================

(defn ^{:added "1.5.0"} add-initial-properties
  "NOTE: Properties were added to production long before 1.5.0. This is to
  provide compatibility with `blueprints.core/conform-db`."
  [conn part]
  (let [licenses (partial property-licenses conn)]
    [(property part "West SoMa"
               "52gilbert"
               #inst "2016-12-01T00:00:00.000-00:00"
               (address "52 Gilbert St.")
               (licenses [1 2300.0] [3 2300.0] [6 2100.0] [12 2000.0])
               (units "52gilbert" 6)
               :slack-channel "#52-gilbert")
     (property part "The Mission"
               "2072mission"
               #inst "2017-04-15T00:00:00.000-00:00"
               (address "2072 Mission St.")
               (licenses [1 2400.0] [3 2400.0] [6 2200.0] [12 2100.0])
               (units "2072mission" 20)
               :slack-channel "#2072-mission")]))


(def ^{:added "1.8.0"} add-managed-ids
  [{:db/id                       [:property/internal-name "52gilbert"]
    :property/managed-account-id "acct_191838JDow24Tc1a"}
   {:db/id                       [:property/internal-name "2072mission"]
    :property/managed-account-id "acct_191838JDow24Tc1a"}])


(def ^{:added "1.10.0"} add-deposit-connect-ids
  [{:db/id                       [:property/code "52gilbert"]
    :property/deposit-connect-id "acct_191838JDow24Tc1a"}
   {:db/id                       [:property/code "2072mission"]
    :property/deposit-connect-id "acct_191838JDow24Tc1a"}])


(defn properties-present?
  "Are the properties to be seeded already present in the database? This is
  needed because the production properties arrived in the db prior to use of
  conformity."
  [conn]
  (let [db (d/db conn)]
    (and (d/entity db [:property/internal-name "52gilbert"])
         (d/entity db [:property/internal-name "2072mission"]))))


(def ^{:added "2.7.0"} add-copy-doc-ids
  [{:db/id            [:property/code "52gilbert"]
    :tipe/document-id "5b1edd5fa98abb0013cb686c"}
   {:db/id            [:property/code "2072mission"]
    :tipe/document-id "5b2aa642175f970013b875d7"}])


(defn ^{:added "2.7.1"} add-additional-communities
  "Ensure that there is seed data to represent every community that is in
  operation (in real life) as of August 2018"
  [conn part]
  (let [licenses (partial property-licenses conn)]
    [(property part "SoMa South Park"
               "414bryant"
               #inst "2018-05-01T00:00:00.000-00:00"
               (address "414 Bryant St.")
               (licenses [1 2200.0] [3 2200.0] [6 2150.0] [12 2100.0])
               (units "414bryant" 15)
               :slack-channel "#414-bryant"
               :tipe-document-id "5b1e8b2520003b0013f1ffb8"
               :cover-image-url "https://s3-us-west-2.amazonaws.com/starcity-images/communities/covers/285873023311132/sp-navbar.jpg")
     (property part "North Beach"
               "6nottingham"
               #inst "2018-01-15T00:00:00.000-00:00"
               (address "6 Nottingham St.")
               (licenses [1 2200.0] [3 2200.0] [6 2150.0] [12 2100.0])
               (units "6nottingham" 11)
               :slack-channel "#6-nottingham"
               :tipe-document-id "5b223b644e888200135a1641"
               :cover-image-url "https://s3-us-west-2.amazonaws.com/starcity-images/6nottingham.jpg")
     (property part "Venice Beach"
               "29navy"
               #inst "2018-01-15T00:00:00.000-00:00"
               {:address/lines "29 Navy St."
                :address/city  "Venice"}
               (licenses [1 2200.0] [3 2200.0] [6 2150.0] [12 2100.0])
               (units "29navy" 31)
               :slack-channel "#29-navy"
               :tipe-document-id "5b47b387e37c18001338c989"
               :cover-image-url "https://s3-us-west-2.amazonaws.com/starcity-images/communities/covers/285873023341680/venice-beach-exterior.jpg")]))


(defn norms [conn part]
  (merge
   {}
   (when-not (properties-present? conn)
     {:blueprints.seed/add-initial-properties
      {:txes [(add-initial-properties conn part)]}
      :blueprints.seed/add-cover-image-urls-09262017
      {:txes [[{:db/id                    [:property/internal-name "52gilbert"]
                :property/cover-image-url "/assets/images/52gilbert.jpg"}
               {:db/id                    [:property/internal-name "2072mission"]
                :property/cover-image-url "/assets/images/2072mission.jpg"}]]}
      :blueprints.seed/add-managed-ids
      {:txes     [add-managed-ids]
       :requires [:blueprints.seed/add-initial-properties]}
      :blueprints.seed/add-deposit-connect-ids
      {:txes     [add-deposit-connect-ids]
       :requires [:blueprints.seed/add-initial-properties]}

      :blueprints.seed/add-copy-doc-ids-06122018
      {:txes     [add-copy-doc-ids]
       :requires [:blueprints.seed/add-initial-properties]}

      :blueprints.seed/add-additional-communities-08132018
      {:txes [(add-additional-communities conn part)]}})))
