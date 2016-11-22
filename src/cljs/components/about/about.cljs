(ns components.about
  (:require [app.state :refer [app-state]]))

(defn component []
  [:div [:h1 "About Page"]
    (str @app-state)])
