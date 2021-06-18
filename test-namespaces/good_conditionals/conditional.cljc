(ns good-conditionals.conditional
  #?@
  (:clj
   [(:require [clojure.java.io :as io]
              [clojure.string :as str])]

   :cljs
   [(:require [clojure.string :as str])]))

(defn hello []
  #?(:clj (io/file "abc.txt"))
  (str/join ["HEL" "LO"]))
