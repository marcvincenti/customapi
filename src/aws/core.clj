(ns aws.core)
            
(def app-name 
  "global app name"
  (atom
    (let [env-name (System/getenv "APP_NAME")]
      (if env-name
        (clojure.string/lower-case env-name)
        nil))))
        
(defn set-app-name
  "Change global app-name"
  [new-name]
  (reset! app-name (clojure.string/lower-case new-name)))
