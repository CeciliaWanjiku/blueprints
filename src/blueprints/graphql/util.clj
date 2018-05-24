(ns blueprints.graphql.util)

(defn pprint [result]
  (letfn [(-prettify [m]
            (reduce
             (fn [acc [k v]]
               (assoc acc k
                      (cond
                        (instance? flatland.ordered.map.OrderedMap v) (into {} (-prettify v))
                        (sequential? v)                               (map -prettify v)
                        :otherwise                                    v)))
             {}
             m))]
    (update result :data -prettify)))
