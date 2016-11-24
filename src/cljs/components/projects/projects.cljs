(ns components.projects
  (:require [reagent.core :as r]
            [app.state :refer [app-state]]))

(defn component []
  [:div [:h1 "Projects Page"]
  [:ul
  (for [p (get-in @app-state [:projects])] ^{:key p}
    [:li (str p)])]])
