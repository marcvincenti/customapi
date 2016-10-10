(defproject clojure-rest "0.1.0-SNAPSHOT"
  :description "Automated Generation Of REST APIs"
  :url "localhost"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-json "0.4.0"]
                 [clj-http "3.0.1"]
                 [amazonica "0.3.67"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:init clojure-rest.db/init!
         :handler clojure-rest.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
