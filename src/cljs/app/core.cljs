(ns app.core
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.core :as r]
            [app.state :refer [app-state]]
            [providers.auth :as auth]
            ;navbar
            [components.menu-bar :as menu-bar]
            ;my pages
            [pages.about :as about]
            [pages.home :as home]
            [pages.login :as login]
            [pages.project-details :as project-details]
            [pages.projects :as projects]))

;Adding Browser History
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;Page routes definition
(defn app-routes []
  (secretary/set-config! :prefix "#")
  (defroute "/" [] (swap! app-state assoc :page :home))
  (defroute "/about" [] (swap! app-state assoc :page :about))
  (defroute "/login" [] (swap! app-state assoc :page :login))
  (defroute "/project/:name" [name] (swap! app-state assoc
                                      :page :project-details :project name))
  (defroute "/projects" [] (swap! app-state assoc :page :projects))
  (hook-browser-navigation!))

;Current-page multimethod : return which page to display based on app-state
(defmulti current-page #(@app-state :page))
(defmethod current-page :home [] [home/component])
(defmethod current-page :about [] [about/component])
(defmethod current-page :login  [] [login/component])
(defmethod current-page :project-details [] [project-details/component])
(defmethod current-page :projects [] [projects/component])
(defmethod current-page :default  [] [:div])

;Root function to run cljs app
(defn ^:export run []
  (app-routes)
  (auth/retrieve-session)
  (r/render [menu-bar/component] (.getElementById js/document "menu-bar"))
  (r/render [current-page]
    (.getElementById js/document "app-container")))
