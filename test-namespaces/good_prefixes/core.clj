(ns good-prefixes.core
  (:require [clojure
             [data :as data]
             [string :as str]]))

(defn hello []
  (str/join ["HE" "LLO"])
  (data/diff {:a 100} {:a 200}))
