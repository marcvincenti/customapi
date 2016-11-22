(ns components.login
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [app.state :refer [app-state]]
            [providers.auth :as auth]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn ^:private login-form []
  (let [region-list (r/atom {})
        get-regions (fn [] (go (swap! region-list assoc :list
                      (get-in (<! (http/get "/api/regions")) [:body :data]))))]
  (get-regions)
  (fn []
    [:div {:class "panel panel-primary"}
      [:div {:class "panel-heading"} "Please Login"]
      [:div {:class "panel-body"}
        [:form {:class "form-horizontal form-group col-sm-12"}
          [:div {:class "form-group"}
            [:input {:type "text" :id "inputAccessKey" :required ""
                     :class "form-control" :placeholder "Access key"
                     :on-change #(swap! app-state assoc-in [:creds :access-key]
                                  (-> % .-target .-value))
                     :value (get-in @app-state [:creds :access-key])}]]
          [:div {:class "form-group"}
            [:input {:type "password" :id "inputSecretKey" :required ""
                     :class "form-control" :placeholder "Secret key"
                     :on-change #(swap! app-state assoc-in [:creds :secret-key]
                                  (-> % .-target .-value ))
                     :value (get-in @app-state [:creds :secret-key])}]]
          [:div {:class "form-group"}
            [:select {:multiple "" :class "form-control" :id "inputRegion"}
              (for [reg (:list @region-list)] ^{:key reg}
                [:option {:value (:value reg)} (:name reg)])]]
          [:div {:class "form-group"}
            [:label [:input {:id "inputRemember" :type "checkbox"
                             :defaultChecked true}]
              " Remember me"]]
          [:div {:class "form-group"}
            [:button {:on-click auth/login
                      :class "btn btn-success btn-block" :type "button"}
              "Login"]]]]])))

(defn component []
  [:div [:h1 "Login Page"]
    [login-form]])
