(ns providers.auth
  (:require [reagent.core :as r]
            [app.state :refer [app-state]]))

(defn login
  "Log the user in"
  [& args]
  "TODO")

(defn logout
  "Log the user out"
  []
  (swap! app-state assoc :user {}))
