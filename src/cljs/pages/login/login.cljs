(ns pages.login
  (:require [reagent.core :as r]
            [app.state :refer [app-state]]
            [components.alerts :as alerts]
            [providers.auth :as auth]))

(defn ^:private login-form []
  (let [remember-me (r/atom true)
        loading (r/atom false)
        error? (r/atom false)]
  (fn []
    [:div {:class "panel panel-primary"}
      [:div {:class "panel-heading"} "Please Login"]
      [:div {:class "panel-body"}
      (alerts/danger error?
        "We are unable to connect you with this credentials.")
        [:form {:class "form-horizontal form-group col-sm-12"}
          [:div {:class "form-group"}
            [:input (into {:type "text" :id "inputAccessKey" :required ""
                     :class "form-control" :placeholder "Access key"
                     :on-change #(swap! app-state assoc-in [:creds :access-key]
                                  (-> % .-target .-value))
                     :value (get-in @app-state [:creds :access-key])}
                     (when @loading {:disabled "disabled"}))]]
          [:div {:class "form-group"}
            [:input (into {:type "password" :id "inputSecretKey" :required ""
                     :class "form-control" :placeholder "Secret key"
                     :on-change #(swap! app-state assoc-in [:creds :secret-key]
                                  (-> % .-target .-value ))
                     :value (get-in @app-state [:creds :secret-key])}
                     (when @loading {:disabled "disabled"}))]]
          [:div {:class "form-group"}
            [:label [:input {:id "inputRemember" :type "checkbox"
                             :defaultChecked @remember-me
                             :on-change #(reset! remember-me
                                (-> % .-target .-value))}]
              " Remember me"]]
          [:div {:class "form-group"}
            [:button {:on-click #(auth/login loading error?) :type "button"
                      :class (str "btn btn-success btn-block"
                        (when @loading " disabled"))}
              (if @loading "Connecting..." "Login")]]]]])))

(defn component []
  [:div [:h1 "Login Page"]
    [login-form]])
