(ns pages.projects
  (:require [reagent.core :as r]
            [app.state :refer [app-state]]
            [providers.projects :as projects]))

(defn ^:private add-project-modal []
  (let [p-name (r/atom nil)]
    (fn []
  [:div
    [:div {:class "modal fade" :id "addProjModal" :tabIndex "-1" :role "dialog"
           :aria-labelledby "addProjectModalLabel"}
      [:div {:class "modal-dialog" :role "document"}
        [:div {:class "modal-content"}
          [:div {:class "modal-header"}
            [:button {:type "button" :class "close"
                      :data-dismiss "modal" :aria-label "Close"}
              [:span {:aria-hidden "true"} "x"]]
            [:h4 {:class "modal-title" :id "addProjectModalLabel"}
              "Create a new project"]]
          [:div {:class "modal-body"}
            [:div {:class "form-horizontal"}
              [:div {:class "form-group"}
                [:label {:class "control-label col-sm-3" :for "nameInput"}
                  "Project name"]
                [:div {:class "col-sm-9"}
                  [:input {:type "text" :class "form-control"
                           :on-change #(reset! p-name (-> % .-target .-value))
                           :value @p-name :id "nameInput"
                           :placeholder "Project name"}]]]]]
          [:div {:class "modal-footer"}
            [:button {:type "button" :class "btn btn-default"
                      :data-dismiss "modal"} "Close"]
            [:button {:type "button" :class "btn btn-primary"
                      :data-dismiss "modal"
                      :on-click #(projects/create-project @p-name)}
              "Create"]]]]]
    [:button {:type "button" :class "btn btn-primary"
              :data-toggle "modal" :data-target "#addProjModal"}
      "Create new Project"]])))

(defn component []
  (let [refreshing (r/atom false)]
    (fn []
    [:div {:class "container"} [:h1 {:class "page-header"} "Projects Page"]
      [:div {:class "btn-toolbar" :role "toolbar"}
        [add-project-modal]
        [:button {:type "button"
                  :class (str "btn btn-default" (when @refreshing " disabled"))
                  :on-click #(projects/get-projects refreshing)} "Refresh"]]
      [:hr]
      (when (not= (count (get-in @app-state [:projects])) 0)
        [:div {:class "panel panel-default"}
          [:div {:class "panel-heading"} "Projects list"]
          [:table {:class "table"}
            [:tbody
              (for [p (get-in @app-state [:projects])] ^{:key p}
                  [:tr
                    [:td [:button {:type "button" :class "btn btn-default"
                                   :aria-label "Remove"
                                   :on-click #(projects/delete-project p)}
                            [:span {:class "glyphicon glyphicon-remove"
                                    :aria-hidden "true"}]]]
                    [:td [:a {:href (str "#/project/" (get p :name))}
                          (get p :name)]]
                    [:td (str p)]])]]])])))
