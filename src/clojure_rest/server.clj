(ns clojure-rest.server
  (:require [clojure-rest.db :as db]))

(defn init!
  ([] (init! :test))
  ([profile]
   (db/set-profile! profile)
   (db/init-db! profile)))
