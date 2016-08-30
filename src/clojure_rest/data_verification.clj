(ns clojure-rest.data-verification)

(defn ^:private run-checks
  "run the multiples checks to do, data is not nil and func is a function or a list of functions"
  [func data]
    (if (vector? func)
      (let [seq-errors (reduce #(conj %1 (run-checks %2 data)) [] (rest func))]
        (case (first func)
          :or (when (not-any? nil? seq-errors) (first seq-errors))
          :and (let [seq-errors-only (remove nil? seq-errors)]
                  (when-not (empty? seq-errors-only) (clojure.string/join "\n" seq-errors-only)))))
      (func data)))

(defn check
  "check all the values passed in parameter with run-checks and return a concatened error string"
  [& args]
    (let [wrong-entries 
      (reduce #(let [{:keys [data function dataname required] 
                        :or {dataname "UNKNOW" required false}} %2
                      ret (if data
                            (run-checks function data)
                            (when required
                              (str "Missing required value : \"" dataname "\".")))]
                  (if ret (conj %1 ret))) [] args)]
      (when-not (empty? wrong-entries)
        (clojure.string/join "\n" wrong-entries))))
      
(defn xstring?
  [x]
  (when-not (string? x) "Not a string."))      
      
(defn xemail-address?
  "Return string error if the email address is not valid, based on RFC 2822."
  [email]
  (if (string? email)
	  (let [re (str "(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+"
					"(?:\\.[a-z0-9!#$%&'*+/=?" "^_`{|}~-]+)*"
					"@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+"
					"[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")]
      (when-not (boolean (re-matches (re-pattern re) email))
        "\"email\" is not RFC 2822 compliant."))
    "\"email\" have to be a string."))
    
(defn xpassword?
  "Return string error if the password isn't a string"
  [pwd]
  (when-not (string? pwd)
    "\"email\" have to be a string."))
    
(defn xusername?
  "Return string error if the username is not valid"
  [uname]
  (if (string? uname)
	  (let [re (str "[a-zA-Z-_ ’'‘ÆÐƎƏƐƔĲŊŒẞÞǷȜæðǝəɛɣĳŋœĸſßþƿȝ"
                  "ĄƁÇĐƊĘĦĮƘŁØƠŞȘŢȚŦŲƯY̨Ƴąɓçđɗęħįƙłøơşșţț"
                  "ŧųưy̨ƴÁÀÂÄǍĂĀÃÅǺĄÆǼǢƁĆĊĈČÇĎḌĐƊÐÉÈĖÊËĚĔ"
                  "ĒĘẸƎƏƐĠĜǦĞĢƔáàâäǎăāãåǻąæǽǣɓćċĉčçďḍđɗð"
                  "éèėêëěĕēęẹǝəɛġĝǧğģɣĤḤĦIÍÌİÎÏǏĬĪĨĮỊĲĴĶ"
                  "ƘĹĻŁĽĿʼNŃN̈ŇÑŅŊÓÒÔÖǑŎŌÕŐỌØǾƠŒĥḥħıíìiîï"
                  "ǐĭīĩįịĳĵķƙĸĺļłľŀŉńn̈ňñņŋóòôöǒŏōõőọøǿơœ"
                  "ŔŘŖŚŜŠŞȘṢẞŤŢṬŦÞÚÙÛÜǓŬŪŨŰŮŲỤƯẂẀŴẄǷÝỲŶŸ"
                  "ȲỸƳŹŻŽẒŕřŗſśŝšşșṣßťţṭŧþúùûüǔŭūũűůųụưẃ"
                  "ẁŵẅƿýỳŷÿȳỹƴźżžẓ0-9]{4,32}")]
      (when-not (boolean (re-matches (re-pattern re) uname))
        "The \"username\" can't contain special characters and has to be between 4 and 32 characters."))
    "\"username\" have to be a string."))
    
(defn xpic-uri?
  "Return a string error if picture uri isn't RFC 2396 compliant."
  [uri]
  (let [re (str "(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*\\.(?:jpg|jpeg|png))(?:\\?([^#]*))?(?:#(.*))?")]
    (when-not (boolean (re-matches (re-pattern re) uri))
      "\"picture\"'s URI is not RFC 2396 compliant.")))

(defn xpic-file? 
  [{:keys [content-type]}]
  (when-not (or (= content-type "image/jpeg") (= content-type "image/png"))
    "The \"picture\" doesn't seem to be a jpeg or a png or an uri."))
    
(defn xpic?
  "return a string error if picture isn't a valid uri or valid file"
  [x]
  (if (string? x)
    (xpic-uri? x)
    (xpic-file? x)))
    
(defn xgender?
  "Return string error if the gender isn't equal to 'male' or 'female'"
  [gender]
  (if (string? gender)
    (let [re (str "male|female")]
      (if (boolean (re-matches (re-pattern re) gender))
        nil
        "\"gender\" have to be equal to 'male' or 'female'."))
    "\"gender\" have to be a string."))

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
    
(defn username?
  "Returns true if the username is correct"
  [uname]
  (if (string? uname)
	  (let [re (str "[a-zA-Z-_ ’'‘ÆÐƎƏƐƔĲŊŒẞÞǷȜæðǝəɛɣĳŋœĸſßþƿȝ"
                  "ĄƁÇĐƊĘĦĮƘŁØƠŞȘŢȚŦŲƯY̨Ƴąɓçđɗęħįƙłøơşșţț"
                  "ŧųưy̨ƴÁÀÂÄǍĂĀÃÅǺĄÆǼǢƁĆĊĈČÇĎḌĐƊÐÉÈĖÊËĚĔ"
                  "ĒĘẸƎƏƐĠĜǦĞĢƔáàâäǎăāãåǻąæǽǣɓćċĉčçďḍđɗð"
                  "éèėêëěĕēęẹǝəɛġĝǧğģɣĤḤĦIÍÌİÎÏǏĬĪĨĮỊĲĴĶ"
                  "ƘĹĻŁĽĿʼNŃN̈ŇÑŅŊÓÒÔÖǑŎŌÕŐỌØǾƠŒĥḥħıíìiîï"
                  "ǐĭīĩįịĳĵķƙĸĺļłľŀŉńn̈ňñņŋóòôöǒŏōõőọøǿơœ"
                  "ŔŘŖŚŜŠŞȘṢẞŤŢṬŦÞÚÙÛÜǓŬŪŨŰŮŲỤƯẂẀŴẄǷÝỲŶŸ"
                  "ȲỸƳŹŻŽẒŕřŗſśŝšşșṣßťţṭŧþúùûüǔŭūũűůųụưẃ"
                  "ẁŵẅƿýỳŷÿȳỹƴźżžẓ0-9]{4,32}")]
      (boolean (re-matches (re-pattern re) uname)))
    false))
    
(defn image-uri?
  "Returns true if picture uri is valid, based on RFC 2396."
  [uri]
  (if (string? uri)
	  (let [re (str "(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*\\.(?:jpg|jpeg|png))(?:\\?([^#]*))?(?:#(.*))?")]
      (boolean (re-matches (re-pattern re) uri)))
    false))

(defn gender?
  "Returns true if the gender is equal to 'male' or 'female'"
  [gender]
  (let [re (str "male|female")]
    (boolean (re-matches (re-pattern re) gender))))



