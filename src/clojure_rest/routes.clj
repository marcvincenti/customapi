(ns clojure-rest.routes
  (:require [compojure.core :refer :all]
            [compojure.route :refer [not-found]]
            [clojure-rest.users :as users]
            [clojure-rest.files :as files]))

(defn ^:private default-page []
  {:status 200 :body "Hello you ;)"})

(defn ^:private make-response-with-error
  [error-msg f & args]
  (let [result (apply f args)]
    (if-not (seq result)
      {:status 500 :body {:error error-msg}}
      {:status 200 :body result})))
      
(defn ^:private make-response-with-error-while-logged
  [token error-msg f & args]
  (let [id (users/get-id token)]
    (if-not (nil? id)
      (apply make-response-with-error error-msg f id args)
      {:status 500 :body {:error "You need to provide a valid token_access"}})))

(defroutes image
  (GET "/:filename"[filename] (files/get-pic filename)))
  
(defroutes oauth
  (GET "/facebook/:token" [token] (users/auth-facebook token))
  (GET "/google/:token" [token] (users/auth-google token)))
  
(defroutes me
  (GET "/" {params :params} (make-response-with-error-while-logged (:access_token params) "An error as occured" users/get)))
  
(defroutes api
  (GET "/" [] (default-page))
  (GET "/logout" {params :params} (make-response-with-error-while-logged (:access_token params) "User not found" users/logout))
  (context "/oauth" [] oauth)
  (context "/image" [] image)
  (not-found {:status 404 :body "Ressource not found :("}))
