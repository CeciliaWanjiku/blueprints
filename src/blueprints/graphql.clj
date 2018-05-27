(ns blueprints.graphql
  (:require [blueprints.graphql.resolvers :as resolvers]
            [clj-time.coerce :as c]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [cheshire.core :as json]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [datomic.api :as d]
            [taoensso.timbre :as timbre]))

(defn- parse-keyword [s]
  (let [[ns' n'] (string/split s #"/")]
    (keyword ns' n')))


(def custom-scalars
  {:scalars
   {:Long
    {:parse     (schema/as-conformer #(Long. %))
     :serialize (schema/as-conformer #(Long. %))}

    :Keyword
    {:parse     (schema/as-conformer
                 (fn [x]
                   (format "%s/%s" (namespace x) (name x))))
     :serialize (schema/as-conformer identity)}

    :Instant
    {:parse     (schema/as-conformer (comp c/to-date c/from-string))
     :serialize (schema/as-conformer identity)}

    :Any
    {:parse     (schema/as-conformer identity)
     :serialize (schema/as-conformer identity)}}})


(defn- read-resource [path]
  (-> (io/resource path)
      slurp
      edn/read-string))


(def entire-schema
  (->> ["enums" "input-objects" "interfaces" "mutations" "objects" "queries"]
       (map #(read-resource (format "graphql/%s.edn" %)))
       (apply merge custom-scalars)))


(defn compile-schema []
  (-> entire-schema
      (util/attach-resolvers (resolvers/resolvers))
      schema/compile))
