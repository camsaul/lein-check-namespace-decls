(defproject lein-check-namespace-decls.test-project "1.0.0"

  :dependencies [[org.clojure/clojure "1.10.0"]]

  :plugins
  [[lein-check-namespace-decls "LATEST"]]

  :profiles
  {
   :good-no-prefixes
   {:source-paths          ^:replace ["good-no-prefixes"]
    :check-namespace-decls {:prefix-rewriting false}}

   :good-prefixes
   {:source-paths ^:replace ["good-prefixes"]}

   :ignore-unused
   {:source-paths          ^:replace ["unused"]
    :check-namespace-decls {:prune-ns-form false}}

   :ignore-paths
   {:source-paths          ^:replace ["invalid-file"]
    :check-namespace-decls {:ignore-paths [#"invalid-file/bad\.clj$"]}}

   :bad-unsorted
   {:source-paths ^:replace ["bad-unsorted"]}

   :bad-unused
   {:source-paths ^:replace ["unused"]}

   :bad-prefixes
   {:source-paths ^:replace ["bad-prefixes"]}

   :bad-invalid-files
   {:source-paths ^:replace ["invalid-file"]}})
