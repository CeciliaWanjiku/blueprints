(ns blueprints.graphql.resolvers
  (:require [datomic.api :as d]
            [blueprints.graphql.authorization :as authorization]
            [blueprints.graphql.resolvers.account :as account]
            [blueprints.graphql.resolvers.application :as application]
            [blueprints.graphql.resolvers.check :as check]
            [blueprints.graphql.resolvers.deposit :as deposit]
            [blueprints.graphql.resolvers.member-license :as member-license]
            [blueprints.graphql.resolvers.note :as note]
            [blueprints.graphql.resolvers.order :as order]
            [blueprints.graphql.resolvers.payment :as payment]
            [blueprints.graphql.resolvers.payment-source :as source]
            [blueprints.graphql.resolvers.property :as property]
            [blueprints.graphql.resolvers.service :as service]
            [blueprints.graphql.resolvers.unit :as unit]
            [toolbelt.datomic :as td]))

(def ^:private util-resolvers
  {:get            (fn [& ks] (fn [_ _ v] (get-in v ks)))
   :keyword/name   (fn [& ks] (fn [_ _ v] (keyword (name (get-in v ks)))))
   :entity/fetch   (fn [k] (fn [{conn :conn} ps _] (d/entity (d/db conn) (k ps))))
   :entity/created (fn [{conn :conn} _ e] (td/created-at (d/db conn) e))
   :entity/updated (fn [{conn :conn} _ e] (td/updated-at (d/db conn) e))})


(defn resolvers []
  (->> (merge
        account/resolvers
        application/resolvers
        check/resolvers
        deposit/resolvers
        source/resolvers
        member-license/resolvers
        note/resolvers
        order/resolvers
        payment/resolvers
        property/resolvers
        service/resolvers
        unit/resolvers)
       (reduce
        (fn [acc [k v]]
          (if (contains? (methods authorization/authorized?) k)
            (assoc acc k (authorization/wrap-authorize k v))
            (assoc acc k v)))
        {})
       (merge util-resolvers)))
