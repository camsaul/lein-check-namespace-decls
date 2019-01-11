(ns core
  (:require [clojure.string :as str]
            [clojure.data :as data]))

(defn hello []
  (str/join ["HE" "LLO"])
  (data/diff {:a 100} {:a 200}))
