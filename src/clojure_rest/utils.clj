(ns clojure-rest.utils
	(:require [clj-time.local :as l]
            [clojure.java.io :only [as-file input-stream output-stream] :as io]
            [crypto.random :as crypto])
  (import javax.imageio.ImageIO
          java.awt.image.BufferedImage
          java.security.SecureRandom
          javax.crypto.SecretKeyFactory
          javax.crypto.spec.PBEKeySpec))

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

(defn generate-token
  "Return a new token access"
  []
  (str (java.util.UUID/randomUUID)))
  
(defn generate-salt
  "Return a new random salt"
  []
  (crypto/bytes 64))
  
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
      
(defn pbkdf2
  "Get a hash for the given string and optional salt"
  [x salt]
  (let [k (PBEKeySpec. (.toCharArray x) (.getBytes salt) 1000 192)
        f (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA1")]
    (format "%x"
      (java.math.BigInteger. (.getEncoded (.generateSecret f k))))))
