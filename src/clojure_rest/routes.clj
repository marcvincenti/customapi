(ns clojure-rest.routes
  (:require [compojure.core :refer :all]
            [compojure.route :refer [not-found]]
            [clojure-rest.wrappers :as wps]
            [clojure-rest.users :as users]))

(defn ^:private default-page []
  {:status 200 :body "Hello you ;)"})
  
(defroutes oauth
  (GET "/facebook/:token" [token] (users/auth-facebook token))
  (GET "/google/:token" [token] (users/auth-google token)))
  
(defroutes testing
  (GET "/username/:uname" [uname] (users/test-username! uname))
  (GET "/email/:email" [email] (users/test-email! email)))
  
(defroutes me
  (POST "/" {params :params} (users/register! params))
  (wps/require-access-token 
    (GET "/" {user-id :user-id} (users/get-my-profile! user-id))))
  
(defroutes api
  (GET "/" [] (default-page))
  (context "/oauth" [] oauth)
  (POST "/login" {params :params} (users/login! params))
  (context "/me" [] me)
  (context "/test" [] testing)
  (not-found {:status 404 :body "Ressource not found :("}))
