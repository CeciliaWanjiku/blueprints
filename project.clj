(defproject starcity/blueprints "3.0.0-SNAPSHOT"
  :description "The Starcity API server."
  :url "https://github.com/starcity-properties/blueprints"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 ;; web
                 [ring "1.6.3"]
                 [starcity/datomic-session-store "0.1.0"]
                 [starcity/customs "1.0.0"]
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [ring-middleware-format "0.7.2"]
                 ;; graphql
                 [com.walmartlabs/lacinia "0.26.0"]
                 [vincit/venia "0.2.5"]
                 ;; db
                 [io.rkn/conformity "0.5.1"]
                 [datomic-schema "1.3.0"]
                 [starcity/toolbelt-datomic "0.5.0"]
                 [starcity/teller "1.1.2"]
                 ;; config
                 [aero "1.1.3"]
                 ;; logging
                 [com.taoensso/timbre "4.10.0"]
                 [starcity/drawknife "1.0.0"]
                 ;; util
                 [starcity/toolbelt-async "0.4.0"]
                 [starcity/toolbelt-core "0.5.0"]
                 [starcity/toolbelt-date "0.3.0"]
                 [mount "0.1.12"]
                 [clj-time "0.14.4"]]


  :jvm-opts ["-server"
             "-Xmx4g"
             "-XX:+UseCompressedOops"
             "-XX:+DoEscapeAnalysis"
             "-XX:+UseConcMarkSweepGC"]

  :repl-options {:init-ns user}

  :plugins [[s3-wagon-private "1.2.0"]]

  :repositories {"releases" {:url        "s3://starjars/releases"
                             :username   :env/aws_access_key
                             :passphrase :env/aws_secret_key}})
