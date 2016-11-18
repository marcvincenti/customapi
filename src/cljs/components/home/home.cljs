(ns components.home
  (:require [app.state :refer [app-state]]
            [components.menu-bar :as menu-bar]))

(defn component []
  [:div [:h1 "Home Page"]
    [:ul
      [:li [:a {:href "#/login"} "login page"]]
      [:li [:a {:href "#/about"} "about page"]]]
    (str (deref app-state))])
