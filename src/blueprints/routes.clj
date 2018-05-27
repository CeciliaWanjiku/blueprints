(ns blueprints.routes
  (:require [blueprints.config :as config]
            [blueprints.graphql :as graph]
            [blueprints.models.account :as account]
            [blueprints.util.auth :refer [unauthorized-handler]]
            [buddy.auth.accessrules :refer [restrict]]
            [compojure.core :as compojure :refer [context defroutes GET POST]]
            [com.walmartlabs.lacinia :refer [execute]]
            [customs.access :as access]
            [customs.auth :as auth]
            [customs.role :as role]
            [datomic.api :as d]
            [ring.util.response :as response]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]
            [taoensso.timbre :as timbre]
            [buddy.auth :as buddy]))


;; ==============================================================================
;; graphql ======================================================================
;; ==============================================================================


(defn extract-graphql-expression [request]
  (case (:request-method request)
    :get  [:query (get-in request [:params :query] "")]
    :post [:mutation (get-in request [:params :mutation] "")]))


(defn ->context
  [{{:keys [conn requester config teller]} :deps}]
  {:conn      conn
   :requester requester
   :config    config
   :teller    teller})


(defn result->status [{:keys [errors] :as result}]
  (cond
    (nil? errors)                                      200
    (tb/find-by #(= :unauthorized (:reason %)) errors) 403
    :otherwise                                         400))


(defn graphql-handler
  [req]
  (let [[op expr] (extract-graphql-expression req)
        result    (execute (get-in req [:deps :graphql])
                           (format "%s %s" (name op) expr)
                           nil
                           (->context req))]
    (-> (response/response result)
        (response/content-type "application/transit+json")
        (response/status (result->status result)))))


;; ==============================================================================
;; history ======================================================================
;; ==============================================================================


(defn- query-history
  [db e]
  (d/q '[:find ?attr ?type ?v ?tx-time ?account
         :in $ ?e
         :where
         [?e ?a ?v ?t true]
         [?a :db/ident ?attr]
         [?a :db/valueType ?_type]
         [?_type :db/ident ?type]
         [?t :db/txInstant ?tx-time]
         [(get-else $ ?t :source/account false) ?account]]
       (d/history db) e))


(defn- resolve-value
  [db type value]
  (if (not= type :db.type/ref)
    value
    (let [e (d/entity db value)]
      (or (:db/ident e) value))))


(defn history
  "Produce a list of all changes to entity `e`, the instant at time in which the
  change occurred, and the user that made the change (if present)."
  [db e]
  (->> (query-history db e)
       (mapv
        (fn [[attr type value tx-time account]]
          (let [value   (resolve-value db type value)
                account (when-let [account (d/entity db account)]
                          {:id   (:db/id account)
                           :name (account/short-name account)})]
            (tb/assoc-when
             {:a attr
              :v value
              :t tx-time}
             :account account))))))


;; ==============================================================================
;; routes =======================================================================
;; ==============================================================================


(def ^:private access-handler
  {:and [access/authenticated-user
         {:or [(access/user-isa role/admin)
               (access/user-isa role/member)
               (access/user-isa role/onboarding)]}]})


(defroutes api

  (GET "/graphql" [] graphql-handler)
  (POST "/graphql" [] graphql-handler)

  (GET "/history/:entity-id" [entity-id]
       (fn [req]
         (let [db (d/db (get-in req [:deps :conn]))]
           (-> (response/response {:data {:history (history db (tb/str->int entity-id))}})
               (response/content-type "application/transit+json"))))))


(defroutes routes

  (context "/" [] (restrict api {:handler  access-handler
                                 :on-error unauthorized-handler})))
