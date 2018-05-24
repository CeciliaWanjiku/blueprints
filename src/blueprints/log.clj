(ns blueprints.log
  (:require [blueprints.config :as config :refer [config]]
            [drawknife.core :as drawknife]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as timbre]))

(defstate logger
  :start (timbre/merge-config!
          (drawknife/configuration (config/log-level config)
                                   (config/log-appender config)
                                   (config/log-file config))))
