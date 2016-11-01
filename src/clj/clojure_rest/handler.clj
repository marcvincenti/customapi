(ns clojure-rest.handler
  (:require [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure-rest.wrappers :as wps]
            [clojure-rest.routes :as routes]))

(def app (->  routes/api
              wps/decode-params
              wrap-multipart-params
              wrap-params
              (wrap-json-response {:pretty true})
              wps/allow-cross-origin))
