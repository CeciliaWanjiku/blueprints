(ns blueprints.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [mount.core :as mount :refer [defstate]]
            [toolbelt.core :as tb]))

(defstate config
  :start (-> (io/resource "config.edn")
             (aero/read-config {:resolver aero/root-resolver
                                :profile  (:env (mount/args))})))



;; ==============================================================================
;; webserver ====================================================================
;; ==============================================================================


(defn webserver-port
  "Produce the port that the weberver should start on."
  [config]
  (tb/str->int (get-in config [:webserver :port])))


(defn cookie-name
  "Session cookie name."
  [config]
  (get-in config [:webserver :cookie-name]))


(defn secure-sessions?
  "Should session cookies be secure?"
  [config]
  (get-in config [:webserver :secure-sessions]))


;; ==============================================================================
;; domain =======================================================================
;; ==============================================================================


(defn root-domain
  "The top-level domain of the application."
  [config]
  (:root-domain config))


;; ==============================================================================
;; datomic ======================================================================
;; ==============================================================================


(defn datomic
  "The Datomic configuration. Contains `:uri` and `:partition`"
  [config]
  (:datomic config))


(defn datomic-uri
  "URI of the Datomic database connection."
  [config]
  (get-in config [:datomic :uri]))


(defn datomic-partition
  "The datomic partition to use"
  [config]
  (get-in config [:datomic :partition]))


;; ==============================================================================
;; nrepl ========================================================================
;; ==============================================================================


(defn nrepl-port
  "Port to run the nrepl server on."
  [config]
  (tb/str->int (get-in config [:nrepl :port])))


;; ==============================================================================
;; mailgun ======================================================================
;; ==============================================================================


(defn mailgun-domain
  [config]
  (get-in config [:mailgun :domain]))


(defn mailgun-sender
  [config]
  (get-in config [:mailgun :sender]))


(defn mailgun-api-key
  [config]
  (get-in config [:mailgun :api-key]))


;; ==============================================================================
;; slack ========================================================================
;; ==============================================================================


(defn slack-webhook-url
  [config]
  (get-in config [:secrets :slack :webhook]))


(defn slack-username
  [config]
  (get-in config [:slack :username]))


;; ==============================================================================
;; stripe =======================================================================
;; ==============================================================================


(defn stripe-public-key
  "The Stripe public key."
  [config]
  (get-in config [:secrets :stripe :public-key]))


(defn stripe-secret-key
  "The Stripe secret key."
  [config]
  (get-in config [:secrets :stripe :secret-key]))


;; ==============================================================================
;; environments =================================================================
;; ==============================================================================


(defn development? [config]
  (= :dev (:env (mount/args))))


(defn staging? [config]
  (= :stage (:env (mount/args))))


(defn production? [config]
  (= :prod (:env (mount/args))))


;; ==============================================================================
;; logging ======================================================================
;; ==============================================================================


(defn log-level
  [config]
  (get-in config [:log :level]))


(defn log-appender
  [config]
  (get-in config [:log :appender]))


(defn log-file
  [config]
  (get-in config [:log :file]))
