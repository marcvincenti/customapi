(ns components.login
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn ^:private login-form []
  (let [region-list (r/atom {})
        get-regions (fn [] (go (swap! region-list assoc :list
                      (get-in (<! (http/get "/api/regions")) [:body :data]))))]
  (get-regions)
  (fn []
    [:form {:class "form-horizontal"}
    [:div {:class "form-group"}
      [:label {:for "inputAccessKey" :class "col-sm-2 control-label"} "Access key"]
      [:div {:class "col-sm-10"}
        [:input {:type "text" :id "inputAccessKey" :class "form-control"
                 :placeholder "Access key" :required ""}]]]
    [:div {:class "form-group"}
      [:label {:for "inputSecretAccessKey" :class "col-sm-2 control-label"} "Secret access key"]
      [:div {:class "col-sm-10"}
        [:input {:type "password" :id "inputSecretAccessKey" :class "form-control"
                 :placeholder "Secret access key" :required ""}]]]
    [:div {:class "form-group"}
      [:label {:for "inputRegion" :class "col-sm-2 control-label"} "Region"]
      [:div {:class "col-sm-10"}
        [:select {:multiple "" :class "form-control" :id "inputRegion"}
          (for [reg (:list @region-list)] ^{:key reg}
            [:option {:value (:value reg)} (:name reg)])]]]
    [:div {:class "form-group"}
      [:div {:class "col-sm-offset-2 col-sm-10"}
        [:button {:type "submit" :class "btn btn-default"} "Login"]]]])))

(defn component []
  [:div [:h1 "Login Page"]
    [:ul
      [:li [:a {:href "#/"} "home page"]]
      [:li [:a {:href "#/about"} "about page"]]]
      [login-form]])
