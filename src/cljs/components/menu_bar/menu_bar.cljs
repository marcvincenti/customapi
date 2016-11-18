(ns components.menu-bar
  (:require [app.state :refer [app-state]]))

(defn component []
  (let [active? (fn [p] (when (= p (:page @app-state)) {:class "active"}))]
    [:nav {:class "navbar navbar-default navbar-fixed-top"}
      [:div {:class "container-fluid"}

      [:div {:class "navbar-header"}
        [:button {:type "button" :class "navbar-toggle collapsed"
                  :data-toggle "collapse" :aria-expanded "false"
                  :data-target "#bs-example-navbar-collapse-1"}
          [:span {:class "icon-bar"}]
          [:span {:class "icon-bar"}]
          [:span {:class "icon-bar"}]]
        [:a {:class "navbar-brand" :href "#/"} "Home"]]

        [:div {:class "collapse navbar-collapse" :id "bs-example-navbar-collapse-1"}

          [:ul {:class "nav navbar-nav"}
            [:li (active? :login) [:a {:href "#/login"} "Login"]]
            [:li (active? :about) [:a {:href "#/about"} "About"]]]

          [:ul {:class "nav navbar-nav navbar-right"}
            [:li {:class "dropdown"}
              [:a {:href "#" :class "dropdown-toggle" :data-toggle "dropdown"
                   :role "button" :aria-haspopup "true" :aria-expanded "false"}
                  "Dropdown" [:span {:class "caret"}]]
              [:ul {:class "nav navbar-nav navbar-right"}
                [:li {:class "dropdown"}
                  [:ul {:class "dropdown-menu"}
                    [:li [:a {:href "#"} "Action"]]
                    [:li [:a {:href "#"} "Action2"]]
                    [:li [:a {:href "#"} "Action3"]]
                    [:li {:role "separator" :class "divider"}]
                    [:li [:a {:href "#"} "Action4"]]]]]]]]]]))
