(ns check-namespace-decls.print
  "Logic related to printing log and error messages."
  (:require [clojure.data :as data]
            [puget.printer :as puget]
            [refactor-nrepl.ns.pprint :as r.pprint-ns]))

(def ^:private debug?
  (Boolean/parseBoolean (System/getenv "DEBUG")))

(defn debug [& args]
  (when debug?
    (apply println args)))

(defn with-color
  {:style/indent 1}
  [color msg]
  (str
   (puget/with-options {:print-color true, :color-scheme {:string [color]}}
     (puget/color-text :string msg))))

(defn exception [msg e]
  (binding [*out* *err*]
    (println (with-color :red msg))
    (puget/cprint e)))

(defn error-diff [filename expected actual]
  (binding [*out* *err*]
    (println
     (str
      (with-color :red (format "ns form has unneeded namespaces or is sorted incorrectly in %s:\n" filename))

      (with-color :magenta "\nExpected ns form to look like:\n")
      (r.pprint-ns/pprint-ns expected)

      (with-color :cyan "\nActual ns form:\n")
      (r.pprint-ns/pprint-ns actual)))

    (let [[only-in-expected only-in-actual] (data/diff expected actual)]
      (when (seq only-in-expected)
        (println (with-color :magenta "\nIn expected but not actual:"))
        (puget/cprint only-in-expected))

      (when (seq only-in-actual)
        (println (with-color :cyan "\nIn actual, but not expected:"))
        (puget/cprint only-in-actual)))

    ;; empty newline at end so next message doesn't run together with this one
    (println)))
