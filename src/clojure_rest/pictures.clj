(ns clojure-rest.pictures
  (:require [amazonica.aws.s3 :refer [get-object put-object]]
            [clojure.java.io :refer [input-stream]]
            [clojure-rest.data-utils :refer [picture-uri? picture-file?]]
            [clojure-rest.db :as db]
            [clojure-rest.utils :as utils]
            [clojure-rest.data-verification :as verif])
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
  [file dir]
  (let [{:keys [tempfile]} file
        out (ByteArrayOutputStream.)
        new-filename (str dir "/" (utils/uid) ".png")]
      (make-thumbnail (input-stream tempfile) out 500)
      (try (future 
          (put-object
            :bucket-name db/bucket
            :key new-filename
            :input-stream (input-stream (.toByteArray out))
            :metadata {:content-type "image/png" 
                       :content-length (count (.toByteArray out))}
            :access-control-list {:grant-permission ["AllUsers" "Read"]}))
         (str "https://s3-" (System/getenv "AMZ_ENDPOINT") ".amazonaws.com/" db/bucket "/" new-filename)
        (catch Exception e nil))))

(defn return-uri
  "take an uri or a file (png/jpg) and return an uri"
  [picture]
  (let [errors (verif/check {:data picture :function [:or picture-uri? picture-file?]})]
    (if (-> errors empty? not) (utils/make-error 400 errors)
      (if (string? picture)
        picture
        (save-pic picture "users")))))
