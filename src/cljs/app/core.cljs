(ns app.core
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.core :as r]
            ;my pages
            [pages.about.page :as about]
            [pages.home.page :as home]))

(def app-state (r/atom {}))

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
  (hook-browser-navigation!))

;Current-page multimethod : return which page to display based on app-state
(defmulti current-page #(@app-state :page))
(defmethod current-page :home [] [home/page])
(defmethod current-page :about [] [about/page])
(defmethod current-page :default [] [:div ])

;Root function to run cljs app
(defn ^:export run []
  (app-routes)
  (r/render [current-page]
    (.getElementById js/document "app-container")))
