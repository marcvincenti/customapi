(ns verifications.data-utils
  (:require [verifications.data-verification :refer [isString? isFile?]]))

(defn email-address?
  "Return string error if the email address is not valid, based on RFC 2822."
  [email]
  (isString? email
    :errmsg "\"email\" is not RFC 2822 compliant."
    :regex (str "(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+"
                "(?:\\.[a-z0-9!#$%&'*+/=?" "^_`{|}~-]+)*"
                "@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+"
                "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")))

(defn username?
  "Return string error if the username is not valid"
  [uname]
  (isString? uname
    :errmsg "The \"username\" can't contain special characters and has to be between 4 and 32 characters."
    :regex (str "[a-zA-Z-_ÆÐƎƏƐƔĲŊŒẞÞǷȜæðǝəɛɣĳŋœĸſßþƿȝ"
                "ĄƁÇĐƊĘĦĮƘŁØƠŞȘŢȚŦŲƯY̨Ƴąɓçđɗęħįƙłøơşșţț"
                "ŧųưy̨ƴÁÀÂÄǍĂĀÃÅǺĄÆǼǢƁĆĊĈČÇĎḌĐƊÐÉÈĖÊËĚĔ"
                "ĒĘẸƎƏƐĠĜǦĞĢƔáàâäǎăāãåǻąæǽǣɓćċĉčçďḍđɗð"
                "éèėêëěĕēęẹǝəɛġĝǧğģɣĤḤĦIÍÌİÎÏǏĬĪĨĮỊĲĴĶ"
                "ƘĹĻŁĽĿʼNŃN̈ŇÑŅŊÓÒÔÖǑŎŌÕŐỌØǾƠŒĥḥħıíìiîï"
                "ǐĭīĩįịĳĵķƙĸĺļłľŀŉńn̈ňñņŋóòôöǒŏōõőọøǿơœ"
                "ŔŘŖŚŜŠŞȘṢẞŤŢṬŦÞÚÙÛÜǓŬŪŨŰŮŲỤƯẂẀŴẄǷÝỲŶŸ"
                "ȲỸƳŹŻŽẒŕřŗſśŝšşșṣßťţṭŧþúùûüǔŭūũűůųụưẃ"
                "ẁŵẅƿýỳŷÿȳỹƴźżžẓ0-9’'‘ ]{4,32}")))

(defn picture-uri?
  "Return a string error if picture uri isn't RFC 2396 compliant."
  [uri]
  (isString? uri
    :errmsg "\"picture\"'s URI is not RFC 2396 compliant."
    :regex (str "(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*"
                "\\.(?:jpg|jpeg|png))(?:\\?([^#]*))?(?:#"
                "(.*))?")))


(defn picture-file?
  [f]
  (isFile? f
    :errmsg "The \"picture\" doesn't seem to be a jpeg or a png or an uri."
    :types ["image/jpeg" "image/png"]))
