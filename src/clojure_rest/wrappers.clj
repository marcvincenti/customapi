(ns clojure-rest.wrappers
  (:require [clojure-rest.utils :as utils]))

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
    (if (get-in request [:params :token])
      (handler request)
      (utils/make-error 403 "No access token provided"))))
