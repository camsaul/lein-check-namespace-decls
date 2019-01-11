(ns leiningen.check-namespace-decls
  (:require [leiningen.core
             [eval :as lein]
             [project :as project]]))

(defn- plugin-dep-vector
  "Get the version of this plugin."
  [{:keys [plugins]}]
  (some
   (fn [[plugin-symb :as dep-vector]]
     (when (= plugin-symb 'lein-check-namespace-decls/lein-check-namespace-decls)
       dep-vector))
   plugins))

(defn- check-namespace-decls-profile
  "Lein profile that includes a dependency for `lein-check-namespace-decls`. Used for merging with the project we're
  checking's profile, so `check-namespace-decls.core` will be available when evaluating in the project itself."
  [project]
  {:dependencies [(plugin-dep-vector project)]})

(defn check-namespace-decls
  "Linter that checks that namespace decls for files in a Leinigen project's `:source-paths` are cleaned the way
  `clj-refactor` would clean them."
  [project]
  (let [project (project/merge-profiles project [(check-namespace-decls-profile project)])]
    (lein/eval-in-project
     project
     `(check-namespace-decls.core/check-namespace-decls '~(:source-paths project) '~(:check-namespace-decls project))
     '(require 'check-namespace-decls.core))))
