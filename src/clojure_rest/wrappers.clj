(ns clojure-rest.wrappers
  (:require [clojure-rest.utils :as utils]
            [clojure-rest.users :as users]))

(defn allow-cross-origin
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"]  "*")
          (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,PUT,POST,DELETE")))))
          
(defn decode-params
  [handler]
  (fn [request]
    (handler 
      (assoc request :params (into {} 
        (for [[k v] (:params request)] 
          [(keyword k) (if (string? v) (java.net.URLDecoder/decode v) v)]))))))
          
(defn require-access-token 
  [handler]
  (fn [request]
    (let [access-token (get-in request [:params :access_token])]
      (if access-token
        (try
          (let [user-id (users/user-from-token access-token)]
            (if user-id
              (handler (assoc request :user-id user-id))
              (utils/make-error 403 "Your token is either perimed or invalid")))
            (catch Exception e (utils/make-error 500 "Unable to request the database.")))
        (utils/make-error 403 "No access token provided")))))
