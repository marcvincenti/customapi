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
    [:form {:class "form-horizontal form-group col-sm-12"}
      [:div {:class "form-group"}
        [:input {:type "text" :id "inputAccessKey" :class "form-control"
                 :placeholder "Access key" :required ""}]]
      [:div {:class "form-group"}
        [:input {:type "password" :id "inputSecretAccessKey"
                 :class "form-control" :placeholder "Secret access key"
                 :required ""}]]
      [:div {:class "form-group"}
        [:select {:multiple "" :class "form-control" :id "inputRegion"}
          (for [reg (:list @region-list)] ^{:key reg}
            [:option {:value (:value reg)} (:name reg)])]]
      [:div {:class "form-group"}
        [:label [:input {:id "inputRemember" :type "checkbox"
                         :defaultChecked true}]
          " Remember me"]]
      [:div {:class "form-group"}
        [:button {:type "submit" :class "btn btn-success btn-block"}
          "Login"]]])))

(defn component []
  [:div [:h1 "Login Page"]
    [:ul
      [:li [:a {:href "#/"} "home page"]]
      [:li [:a {:href "#/about"} "about page"]]]
      [login-form]])
