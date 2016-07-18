(ns clojure-rest.routes
  (:require [compojure.core :refer :all]
            [compojure.route :refer [not-found]]
            [clojure-rest.users :as users]))

(defn ^:private default-page []
  {:status 200 :body "Hello you ;)"})
  
(defroutes oauth
  (GET "/facebook/:token" [token] (users/auth-facebook token))
  (GET "/google/:token" [token] (users/auth-google token)))
  
(defroutes me
  (POST "/subscribe" {params :params} (users/register! params))
  (POST "/login" {params :params} (users/login! params)))
  
(defroutes api
  (GET "/" [] (default-page))
  (context "/oauth" [] oauth)
  (context "/me" [] me)
  (not-found {:status 404 :body "Ressource not found :("}))
