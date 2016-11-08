(defproject rapidframework "0.1.0-SNAPSHOT"
  :description "Automated Generation Of REST APIs"
  :url "localhost"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [compojure "1.5.1"]      ;ring wrapper
                 [reagent "0.6.0"]        ;react.js
                 [cljs-http "0.1.42"]     ;for ajax calls
                 [secretary "1.2.3"]      ;router for cljs
                 [ring/ring-json "0.4.0"] ;ring server
                 [amazonica "0.3.67"]]    ;aws java sdk clj wrapper
  :plugins [[lein-ring "0.9.7"]
            [lein-cljsbuild "1.1.4"]]
  :ring {:init clojure-rest.init/init!
         :handler clojure-rest.handler/app}
  :cljsbuild {:builds [{:id           "dev"
                        :source-paths ["src/cljs/"]
                        :compiler     {:main app.core
                                       :asset-path "js/out"
                                       :output-to "resources/public/js/app.js"
                                       :output-dir "resources/public/js/out"}}
                       {:id           "prod"
                        :source-paths ["src/cljs/"]
                        :compiler {:main            app.core
                                   :output-to       "resources/public/js/app.js"
                                   :optimizations   :advanced
                                   :closure-defines {goog.DEBUG false}
                                   :pretty-print    false}}]}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                 [ring/ring-mock "0.3.0"]]}})
