(ns clojure-rest.routes
  (:require [compojure.core :refer :all]
            [compojure.route :refer [not-found]]
            [clojure-rest.wrappers :as wps]))

(defn ^:private default-page []
  {:status 200 :body "Hello you ;)"})
  
(defn ^:private four-oh-four-page []
  {:status 404 :body "Ressource not found :("})
  
(defroutes api
  (GET "/" [] (default-page))
  (not-found (four-oh-four-page)))
