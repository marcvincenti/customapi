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
  (wps/require-access-token 
    (GET "/" {params :params user-id :user-id} {:body (str user-id)}))
  (POST "/subscribe" {params :params} (users/register! params))
  (POST "/login" {params :params} (users/login! params)))
  
(defroutes api
  (GET "/" [] (default-page))
  (context "/oauth" [] oauth)
  (context "/me" [] me)
  (context "/test" [] testing)
  (not-found {:status 404 :body "Ressource not found :("}))
