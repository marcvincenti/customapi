(ns components.about)

(defn component []
  [:div [:h1 "About Page"]
    [:ul
      [:li [:a {:href "#/"} "home page"]]
      [:li [:a {:href "#/login"} "login page"]]]])
