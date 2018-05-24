(ns blueprints.core
  (:require [blueprints.datomic]
            [blueprints.log]
            [blueprints.nrepl]
            [blueprints.graphql]
            [blueprints.teller]))


(comment

  (defn conform-db
   "Conform novelty to db over `conn`."
   [conn & [part]]
   (let [part (or part default-partition)]
     (schema/conform conn part)
     (seed/conform conn part))))
