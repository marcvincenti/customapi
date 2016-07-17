(ns clojure-rest.pictures
  (:require [amazonica.aws.s3 :refer [get-object put-object]]
            [clojure.java.io :refer [input-stream]]
            [clojure-rest.db :as db]
            [clojure-rest.utils :as utils]
            [clojure-rest.valid :as valid])
  (:import java.io.ByteArrayOutputStream
           javax.imageio.ImageIO
           java.awt.image.BufferedImage))
  
(defn ^:private make-thumbnail
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

(defn save-pic
  "Uploads a picture to S3, return a public uri to the file"
  [file]
  (let [{:keys [tempfile]} file
        out (ByteArrayOutputStream.)
        new-filename (str (utils/uid) ".png")]
      (make-thumbnail (input-stream tempfile) out 500)
      (try 
        (future (put-object @db/conn
            :bucket-name (:users-profiles db/buckets)
            :key new-filename
            :input-stream (input-stream (.toByteArray out))))
         new-filename
        (catch Exception e nil))))

(defn return-uri
  "take an uri or a file (png/jpg) and return an uri"
  [picture]
  (if (or (nil? picture) (valid/image-uri? picture))
    picture
    (if (valid/pic-file? picture)
      (save-pic picture)
      nil)))
