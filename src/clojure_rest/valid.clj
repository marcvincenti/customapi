(ns clojure-rest.valid)

(defn pic-file? 
  [x]
  (if (nil? x)
    false
    (or 
      (= (:content-type x) "image/jpeg")
      (= (:content-type x) "image/png"))))

(defn email-address?
  "Returns true if the email address is valid, based on RFC 2822."
  [email]
  (if (string? email)
	  (let [re (str "(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+"
					"(?:\\.[a-z0-9!#$%&'*+/=?" "^_`{|}~-]+)*"
					"@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+"
					"[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")]
      (boolean (re-matches (re-pattern re) email)))
    false))
    
(defn image-uri?
  "Returns true if picture uri is valid, based on RFC 2396."
  [uri]
  (if (string? uri)
	  (let [re (str "^https?://(?:[a-z0-9-]+.)+[a-z]{2,6}(?:/[^/#?]+)+.(?:jpg|png|jpeg)$")]
      (boolean (re-matches (re-pattern re) uri)))
    false))

(defn gender?
  "Returns true if the gender is equal to 'male' or 'female'"
  [gender]
  (let [re (str "male|female")]
    (boolean (re-matches (re-pattern re) gender))))
