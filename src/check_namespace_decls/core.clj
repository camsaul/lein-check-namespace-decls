(ns check-namespace-decls.core
  (:require [check-namespace-decls.print :as print]
            [clojure.java.io :as io]
            [clojure.tools.namespace.find :as ns.find]
            [clojure.tools.namespace.parse :as ns.parse]
            [refactor-nrepl.config :as r.config]
            [refactor-nrepl.ns.clean-ns :as r.clean-ns])
  (:import [java.io File PushbackReader]))

(defn- actual-decl
  "Read the actual `ns` decl from a file."
  [filename]
  (with-open [reader (PushbackReader. (io/reader filename))]
    (let [decl (ns.parse/read-ns-decl reader {:read-cond :preserve})]
      (mapcat (fn [form]
                (if (reader-conditional? form)
                  [(symbol (if (:splicing? form) "#?@" "#?")) (:form form)]
                  [form]))
              decl))))

(defn- cleaned-decl
  "Read `ns` decl from a file and return cleaned form iff different from the actual form. If the actual form is already
  cleaned, returns `nil`."
  [filename]
  (load-file filename)
  (let [cleaned (r.clean-ns/clean-ns {:path filename})]
    (when-not (= cleaned (actual-decl filename))
      cleaned)))

(defn- file-error
  "Returns map with info about the error if the `ns` declaration if it isn't cleaned; returns `nil` if it is cleaned.
  When env var `DEBUG` is true, prints a message for each file it checks."
  [filename]
  (print/debug "Checking" filename)
  (try
    ;; if cleaned ns decl is different from what's already there print message about it being unclean and return the
    ;; error
    (if-let [cleaned (cleaned-decl filename)]
      (do
        (print/error-diff filename cleaned (actual-decl filename))
        {:expected cleaned, :actual (actual-decl filename)})
      ;; otherwise if already clean nothing to return
      nil)
    ;; if we couldn't parse the file for one reason or another log the Exception and return the Exception
    (catch Throwable e
      (print/exception (format "Error parsing %s" filename) e)
      {:error e})))

(defn- ignore-file?
  "Check whether we should skip filename because it matches a pattern in the `:ignore-paths` in the configuration. If
  env var `DEBUG` is true, prints message about file being ignored and the pattern that matched it."
  [filename]
  ;; look at each regex in `:ignore-paths`, and if we find one that matches the filename return `true`
  (when-let [matching-pattern (some
                               #(when (re-find % filename) %)
                               (:ignore-paths r.config/*config*))]
    (print/debug "Ignoring" filename "because it matches regex" matching-pattern)
    true))

(defn errors
  "Returns map of source filename -> error info for files that failed the check, or `nil` if there are no errors."
  [source-paths custom-config]
  ;; keep count of each file, checked and each file with an error
  (let [total  (atom 0)
        errors (atom {})
        config (merge r.config/*config* custom-config)]
    (print/debug "Using config:" config (list 'merge r.config/*config* custom-config))
    (binding [r.config/*config* config]
      (doseq [source-path       source-paths
              :let              [source-dir (io/file source-path)]
              :when             (.isDirectory source-dir)
              ^File source-file (ns.find/find-clojure-sources-in-dir source-dir)
              :let              [filename (.getCanonicalPath source-file)]
              :when             (not (ignore-file? filename))]
        (swap! total inc)
        (when-let [error (file-error filename)]
          (swap! errors assoc filename error)))
      (println (print/with-color :blue (format "Checked %d files; %d had errors." @total (count @errors))))
      (not-empty @errors))))

(defn- check-namespace-decls* [source-paths config]
  (println "Checking namespace declarations...")
  (when (errors source-paths config)
    (System/exit 1))
  (println "All namespace declarations are OK. Good work!")
  (System/exit 0))

(defn check-namespace-decls
  "Check that all `ns` declarations in `source-paths` are cleaned and exit with a status of 0 if all namespaces are
  clean; otherwise print error messages for each improperly formatted ns declaration and exit with nonzero status."
  ;; one-arity version: for deps.edn usage
  ([config]
   (check-namespace-decls (:source-paths config) (dissoc config :source-paths)))
  ;; two-arity version: for lein plugin usage
  ([source-paths config]
   (try
     (check-namespace-decls* source-paths config)
     (catch Throwable e
       (print/exception "Exception running check-namespace-decls:" e)
       ;; exit with a 2 instead of the usual 1 so tests can differentiate between a successful run that found bad files
       ;; vs a run that couldn't finish
       (System/exit 2)))))
