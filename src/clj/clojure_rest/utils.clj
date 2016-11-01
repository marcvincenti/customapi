(ns clojure-rest.utils)

(defn str->int
  "Converts string to int. Throws an exception if s cannot be parsed as an int"
  [s]
 (Integer/parseInt s))

(defn str->float
  "Converts string to float. Throws an exception if s cannot be parsed as a
   float"
  [s]
  (Float/parseFloat s))

(defn blurp
  "Like slurp but with binary files"
  [filename]
  (java.io.File. filename))

(defn make-error
  "Make an error response"
  [code msg]
  {:status code
   :body msg})

(defn timestamp
  "Return the actual unix timestamp in second"
  []
  (quot (System/currentTimeMillis) 1000))

(defn uid
  "Generate an uid"
  []
  (.replaceAll (.toString (java.util.UUID/randomUUID)) "-" ""))
