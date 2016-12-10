(ns pages.project-details
  (:require [app.state :refer [app-state]]))


(defn component []
  [:div {:class "container"}
    [:h1 {:class "page-header"} (:project @app-state)]])
