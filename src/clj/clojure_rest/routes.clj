(ns clojure-rest.routes
  (:require [compojure.core :refer [GET POST DELETE defroutes context]]
            [compojure.route :refer [not-found resources]]
            [ring.util.response :refer [resource-response]]
            [aws.apis :as apis]
            [aws.utils :as utils]
            [clojure-rest.wrappers :as wps]))

(defn ^:private four-oh-four-page []
  {:status 404 :body "Ressource not found :("})

(defroutes ^:private projects
  (GET "/" {params :params} (apis/get-projects params))
  (POST  "/" {params :params} (apis/create-project params))
  (DELETE  "/" {params :params} (apis/delete-project params)))

(defroutes ^:private api
  (POST "/login" {params :params} (apis/get-projects params))
  (GET  "/regions" [] (utils/list-regions))
  (context "/projects" [] projects))

(defroutes app
  (GET  "/" [] (resource-response "index.html" {:root "public"}))
  (resources "/")
  (context "/api" [] api)
  (not-found (four-oh-four-page)))
