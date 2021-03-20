(defproject lein-check-namespace-decls "1.0.3"
  :min-lein-version "2.5.0"

  :dependencies
  [[org.clojure/tools.namespace "1.1.0"]
   [cider/cider-nrepl "0.25.9"]
   [mvxcvi/puget "1.3.1"]
   [refactor-nrepl "2.5.1"]]

  :description
  "Linter that checks that namespace decls for files in a Leinigen project's `:source-paths` are cleaned the way
  `clj-refactor` would clean them."

  :license
  {:name "EPL-2.0"
   :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :profiles
  {:dev
   {:dependencies      [[org.clojure/clojure "1.10.3"]]
    :eval-in-leiningen false}}

  :eval-in-leiningen true)
