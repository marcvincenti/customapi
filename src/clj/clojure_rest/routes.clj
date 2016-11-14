(ns clojure-rest.routes
  (:require [compojure.core :refer [GET POST defroutes context]]
            [compojure.route :refer [not-found resources]]
            [ring.util.response :refer [resource-response]]
            [aws.core :as aws]
            [clojure-rest.wrappers :as wps]))

(defn ^:private four-oh-four-page []
  {:status 404 :body "Ressource not found :("})

(defroutes ^:private api
  (POST "/login" {params :params} (aws/get-projects params))
  (GET  "/regions" [] (aws/list-regions)))

(defroutes app
  (GET  "/" [] (resource-response "index.html" {:root "public"}))
  (resources "/")
  (context "/api" [] api)
  (not-found (four-oh-four-page)))
