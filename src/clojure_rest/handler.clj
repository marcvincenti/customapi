(ns clojure-rest.handler
  (:require [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [clojure-rest.routes :as routes]))

(defn ^:private allow-cross-origin
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"]  "*")
          (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,PUT,POST,DELETE")))))

(def app (->  routes/api
              wrap-keyword-params
              wrap-multipart-params
              wrap-params
              (wrap-json-response {:pretty true})
			  allow-cross-origin ))
