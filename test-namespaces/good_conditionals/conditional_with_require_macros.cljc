(ns good-conditionals.conditional-with-require-macros
  (:require [clojure.string :as str])
  #?(:cljs (:require-macros [good-conditionals.conditional-with-require-macros :refer [my-when]])))

#?(:clj
   (defmacro my-when [x]
     `(when true
        ~x)))

(defn hello []
  (my-when (println (str/join) "H" "ello")))
