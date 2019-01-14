(ns core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn hello []
  (io/file "abc.txt")
  (str/join ["HEL" "LO"]))
