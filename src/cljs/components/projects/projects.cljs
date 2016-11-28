(ns components.projects
  (:require [reagent.core :as r]
            [app.state :refer [app-state]]
            [providers.projects :as projects]))

(defn component []
  (let [refreshing (r/atom false)]
    (fn []
    [:div [:h1 "Projects Page"]
      [:ul
        (for [p (get-in @app-state [:projects])] ^{:key p}
          [:li (str p)])]
      [:button {:type "button"
                :class (str "btn btn-default" (when @refreshing " disabled"))
                :on-click #(projects/get-projects refreshing)} "Refresh"]
      [:button {:type "button" :class "btn btn-primary"}
        "Create (not working yet)"]])))
