(ns pages.projects
  (:require [reagent.core :as r]
            [app.state :refer [app-state]]
            [providers.projects :as projects]))

(defn add-project-modal [id-modal]
  [:div {:class "modal fade" :id id-modal :tabIndex "-1" :role "dialog"
         :aria-labelledby "addProjectModalLabel"}
    [:div {:class "modal-dialog" :role "document"}
      [:div {:class "modal-content"}
        [:div {:class "modal-header"}
          [:button {:type "button" :class "close"
                    :data-dismiss "modal" :aria-label "Close"}
            [:span {:aria-hidden "true"} "x"]]
          [:h4 {:class "modal-title" :id "addProjectModalLabel"}
            "Create a new project"]]
        [:div {:class "modal-body"} "TODO..."]
        [:div {:class "modal-footer"}
          [:button {:type "button" :class "btn btn-default"
                    :data-dismiss "modal"} "Close"]
          [:button {:type "button" :class "btn btn-primary"}
            "Save changes"]]]]])

(defn component []
  (let [add-modal "addProjectModal"
        refreshing (r/atom false)]
    (fn []
    [:div [:h1 "Projects Page"]
      (add-project-modal add-modal)
      [:ul
        (for [p (get-in @app-state [:projects])] ^{:key p}
          [:li (str p)])]
      [:button {:type "button"
                :class (str "btn btn-default" (when @refreshing " disabled"))
                :on-click #(projects/get-projects refreshing)} "Refresh"]
      [:button {:type "button" :class "btn btn-primary"
                :data-toggle "modal" :data-target (str "#" add-modal)}
        "Create"]])))
