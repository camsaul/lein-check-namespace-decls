(ns core
  (:require [clojure.data :as data]
            [clojure.string :as str]))

(defn hello []
  (io/file "abc.txt")
  (str/join ["HEL" "LO"]))
