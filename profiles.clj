{:dev {:source-paths ["src" "test" "env/dev"]
       :dependencies [[com.datomic/datomic-free "0.9.5544"]
                      ;; [starcity/reactor "1.10.0"]
                      ]}

 :uberjar {:aot          :all
           :main         blueprints.core
           :source-paths ["src" "env/live"]

           :dependencies [[com.datomic/datomic-pro "0.9.5544"]
                          [org.postgresql/postgresql "9.4.1211"]]

           :repositories {"my.datomic.com" {:url      "https://my.datomic.com/repo"
                                            :username :env/datomic_username
                                            :password :env/datomic_password}}}}
