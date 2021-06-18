(ns good-refer-all.core
  (:require [good-refer-all.other-namespace :refer :all]))

(defn hello []
  (add x y))
