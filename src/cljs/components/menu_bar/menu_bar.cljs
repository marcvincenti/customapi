(ns components.menu-bar
  (:require [app.state :refer [app-state]]
            [providers.auth :as auth]))

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
            [:li (active? :home) [:a {:href "#/"} "Home"]]
            [:li (active? :about) [:a {:href "#/about"} "About"]]]

          [:ul {:class "nav navbar-nav navbar-right"}
            (if (:connected @app-state)
              [:li [:a {:href "#/" :on-click auth/logout} "Logout"]]
              [:li (active? :login) [:a {:href "#/login"} "Login"]])
            ]]]]))
