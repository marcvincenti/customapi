(ns clojure-rest.utils
	(:require [clojure.java.io :only [as-file input-stream output-stream] :as io])
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
