(ns clojure-rest.valid)

(defn email-address?
  "Returns true if the email address is valid, based on RFC 2822."
  [email]
  {:pre [(string? email)]}
  (let [re (str "(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+"
                "(?:\\.[a-z0-9!#$%&'*+/=?" "^_`{|}~-]+)*"
                "@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+"
                "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")]
    (boolean (re-matches (re-pattern re) email))))

(defn gender?
  "Returns true if the gender is equal to 'male' or 'female'"
  [gender]
  (let [re (str "male|female")]
    (boolean (re-matches (re-pattern re) gender))))
