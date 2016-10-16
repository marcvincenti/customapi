(ns aws.data)

(def app-name 
  "global app name"
  (atom
    (let [env-name (System/getenv "APP_NAME")]
      (if env-name
        (clojure.string/lower-case env-name)
        nil))))
