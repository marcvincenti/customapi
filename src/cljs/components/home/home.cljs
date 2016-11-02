(ns components.home)

(defn component []
  [:div [:h1 "Home Page"]
    [:ul
      [:li [:a {:href "#/login"} "login page"]]
      [:li [:a {:href "#/about"} "about page"]]]])
