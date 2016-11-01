(ns clojure-rest.routes
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.util.response :refer [resource-response]]
            [clojure-rest.wrappers :as wps]))

(defn ^:private four-oh-four-page []
  {:status 404 :body "Ressource not found :("})

(defroutes api
  (GET  "/" [] (resource-response "index.html" {:root "public"}))
  (resources "/")
  (not-found (four-oh-four-page)))
