(ns components.login
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn ^:private login
  "Log a user with AWS credentials"
  [access-key secret-access-key]
  (go (let [response (<! (http/post "/api/login"
            {:form-params {:access-key access-key
                           :secret-key secret-access-key}}))]
    (if (:success response)
      (.log js/console "OK")
      (.log js/console "Not OK")))))

(defn ^:private login-form []
  (let [region-list (r/atom {})
        access-key (r/atom "")
        secret-key (r/atom "")
        get-regions (fn [] (go (swap! region-list assoc :list
                      (get-in (<! (http/get "/api/regions")) [:body :data]))))]
  (get-regions)
  (fn []
    [:form {:class "form-horizontal form-group col-sm-12"}
      [:div {:class "form-group"}
        [:input {:type "text" :id "inputAccessKey" :class "form-control"
                 :on-change #(reset! access-key (-> % .-target .-value ))
                 :placeholder "Access key" :value @access-key :required ""}]]
      [:div {:class "form-group"}
        [:input {:type "password" :id "inputSecretKey"
                 :class "form-control" :placeholder "Secret key"
                 :on-change #(reset! secret-key (-> % .-target .-value ))
                 :required "" :value @secret-key}]]
      [:div {:class "form-group"}
        [:select {:multiple "" :class "form-control" :id "inputRegion"}
          (for [reg (:list @region-list)] ^{:key reg}
            [:option {:value (:value reg)} (:name reg)])]]
      [:div {:class "form-group"}
        [:label [:input {:id "inputRemember" :type "checkbox"
                         :defaultChecked true}]
          " Remember me"]]
      [:div {:class "form-group"}
        [:button {:on-click #(login @access-key @secret-key)
                  :class "btn btn-success btn-block"}
          "Login"]]])))

(defn component []
  [:div [:h1 "Login Page"]
    [:ul
      [:li [:a {:href "#/"} "home page"]]
      [:li [:a {:href "#/about"} "about page"]]]
      [login-form]])
