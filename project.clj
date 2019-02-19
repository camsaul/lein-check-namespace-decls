(defproject lein-check-namespace-decls "1.0.2"
  :min-lein-version "2.5.0"

  :dependencies
  [[org.clojure/tools.namespace "0.2.11"
    :exclusions [org.clojure/clojure]]
   [mvxcvi/puget "1.1.0"]
   [refactor-nrepl "2.4.0"]]

  :description
  "Linter that checks that namespace decls for files in a Leinigen project's `:source-paths` are cleaned the way
  `clj-refactor` would clean them."

  :license
  {:name "EPL-2.0"
   :url "https://www.eclipse.org/legal/epl-2.0/"}

  :eval-in-leiningen true)
