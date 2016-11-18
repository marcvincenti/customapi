(ns components.menu-bar
  (:require [app.state :refer [app-state]]))

(defn component []
  [:nav {:class "navbar navbar-default navbar-fixed-top"}
    [:div {:class "container-fluid"}

    [:div {:class "navbar-header"}
      [:button {:type "button" :class "navbar-toggle collapsed" :data-toggle "collapse" :data-target "#bs-example-navbar-collapse-1" :aria-expanded "false"}
        [:span {:class "sr-only"} "Toggle navigation"]
        [:span {:class "icon-bar"}]
        [:span {:class "icon-bar"}]
        [:span {:class "icon-bar"}]]
      [:a {:class "navbar-brand" :href "#"} "Brand"]]

      [:div {:class "collapse navbar-collapse" :id "bs-example-navbar-collapse-1"}

        [:ul {:class "nav navbar-nav"}
          [:li {:class "active"}
            [:a {:href "#/"} "Link" [:span {:class "sr-only"} "(current)"]]]
          [:li [:a {:href "#/"} "Link"]]]

        [:ul {:class "nav navbar-nav navbar-right"}
          [:li {:class "dropdown"}
            [:a {:href "#" :class "dropdown-toggle" :data-toggle "dropdown" :role "button" :aria-haspopup "true" :aria-expanded "false"} "Dropdown" [:span {:class "caret"}]]
            [:ul {:class "nav navbar-nav navbar-right"}
              [:li {:class "dropdown"}
                [:ul {:class "dropdown-menu"}
                  [:li [:a {:href "#"} "Action"]]
                  [:li [:a {:href "#"} "Action2"]]
                  [:li [:a {:href "#"} "Action3"]]
                  [:li {:role "separator" :class "divider"}]
                  [:li [:a {:href "#"} "Action4"]]]]]]]]]])
