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
    (GET "/" {user-id :user-id} (users/get-my-profile! user-id)))
  (wps/require-access-token 
    (PUT "/" {user-id :user-id params :params} (users/update! params user-id)))
  (wps/require-access-token 
    (DELETE "/" {user-id :user-id params :params} (users/delete-profile! user-id params))))
  
(defroutes api
  (GET "/" [] (default-page))
  (context "/oauth" [] oauth)
  (context "/me" [] me)
  (context "/test" [] testing)
  (POST "/login" {params :params} (users/login! params))
  (wps/require-access-token 
    (POST "/logout" {user-id :user-id} (users/logout! user-id)))
  (not-found {:status 404 :body "Ressource not found :("}))
