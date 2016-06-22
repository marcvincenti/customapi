(ns clojure-rest.utils
	(:require [clj-time.core :as time]
            [clj-time.local :as l]
            [clojure.java.io :only [as-file input-stream output-stream] :as io]
            [ring.util.response :refer [response]])
  (import javax.imageio.ImageIO
          java.awt.image.BufferedImage))

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
  
(defn current-iso-8601-date
  "Returns current ISO 8601 compliant date."
  []
  (l/format-local-time (l/local-now) :basic-date-time))

(defn to-date
  "take an ISO 8601 compliant date and return a date object"
  [date]
  ;TODO, add support for timezone in BDD and set date to :basic-date-time
  (l/format-local-time date :date-hour-minute-second-ms))

(defn generate-token
  "Return a new token access"
  []
  (str (java.util.UUID/randomUUID)))
  
(defn generate-salt
  "Return a new random salt"
  []
  (+ 1000000 (rand-int 9000000)))
  
(defn make-error
  "Make an error response"
  [code msg]
  {:status code
   :body msg})
    
(defn make-thumbnail
  "Make a png thumbnail with streams"
  [in-stream out-stream width]
  (let [img (ImageIO/read in-stream)
        width (min (.getWidth img) width)
        height (* (/ width (.getWidth img)) (.getHeight img))
        simg (BufferedImage. width height
                             (BufferedImage/TYPE_INT_ARGB))
        g (.createGraphics simg)]
    (do
      (.drawImage g img 0 0 width height nil)
      (.dispose g)
      (ImageIO/write simg "png" out-stream))))
