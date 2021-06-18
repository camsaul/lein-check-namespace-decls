(ns bad-unused.unused.core
  (:require [clojure
             [data :as data]
             [string :as str]]
            [clojure.java.io :as io]))

(defn hello []
  (io/file "abc.txt")
  (data/diff {:a 100} {:a 200}))
