(ns components.login)

(defn login-form []
  [:form {:class "form-horizontal"}
  [:div {:class "form-group"}
    [:label {:for "inputEmail" :class "col-sm-2 control-label"} "Email"]
    [:div {:class "col-sm-10"}
      [:input {:type "text" :id "inputEmail" :class "form-control"
               :placeholder "Email address" :required ""}]]]

  [:div {:class "form-group"}
    [:label {:for "inputPassword" :class "col-sm-2 control-label"} "Password"]
    [:div {:class "col-sm-10"}
      [:input {:type "password" :id "inputPassword" :class "form-control"
               :placeholder "Password" :required ""}]]]
  [:div {:class "form-group"}
    [:label {:for "inputRegion" :class "col-sm-2 control-label"} "Region"]
    [:div {:class "col-sm-10"}
      [:select {:multiple "" :class "form-control" :id "inputRegion"}
        [:option "1"]
        [:option "2"]
        [:option "3"]
        [:option "4"]
        [:option "5"]]]]

  [:div {:class "form-group"}
    [:div {:class "col-sm-offset-2 col-sm-10"}
      [:button {:type "submit" :class "btn btn-default"} "Login"]]]])

(defn component []
  [:div [:h1 "Login Page"]
    [:ul
      [:li [:a {:href "#/"} "home page"]]
      [:li [:a {:href "#/about"} "about page"]]]
      [login-form]])
