(ns clojure-rest.handler
  (:require [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure-rest.wrappers :as wps]
            [clojure-rest.routes :as routes]))

(def app (->  routes/app
              ;wps/decode-params
              ;wrap-multipart-params
              wrap-keyword-params
              wrap-params
              wrap-json-response
              ;wps/allow-cross-origin
              ))
