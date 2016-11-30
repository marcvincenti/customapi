(ns pages.about
  (:require [app.state :refer [app-state]]))

(defn component []
  [:div [:h1 "About Page"]
    [:p "For DEBUG purpose : "
      (str @app-state)]])
