(defproject clojure-rest "0.1.0-SNAPSHOT"
  :description "REST API"
  :url "localhost"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.0"]
                 [ring/ring-json "0.4.0"]
                 [org.clojure/java.jdbc "0.6.0"]
                 [java-jdbc/dsl "0.1.3"]
                 [mysql/mysql-connector-java "6.0.2"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.1"]
                 [clj-http "3.0.1"]
                 [crypto-random "1.2.0"]
                 [amazonica "0.3.57"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:init clojure-rest.server/init!
         :handler clojure-rest.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
