(ns blueprints.graphql.resolvers.util
  (:require [clojure.spec.alpha :as s]
            [toolbelt.core :as tb]))

(defn error-message [t]
  (or (:message (ex-data t)) (.getMessage t) "Unknown error!"))

(s/fdef error-message
        :args (s/cat :throwable tb/throwable?)
        :ret string?)
