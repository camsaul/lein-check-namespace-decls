(ns bad-unsorted.core
  (:require [clojure
             [string :as str]
             [data :as data]]))

(defn hello []
  (str/join ["HE" "LLO"])
  (data/diff {:a 100} {:a 200}))
