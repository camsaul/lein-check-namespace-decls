(ns check-namespace-decls.core-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [leiningen.check-namespace-decls :as check-decls]
            [leiningen.core.project :as lein.project]
            [puget.printer :as puget]))

(defn- project-root [test-dir]
  (.getAbsolutePath (io/file (str "test-namespaces/" test-dir))))

(defn- lein-project [test-dir options]
  (lein.project/make
   {:check-namespace-decls options
    :dependencies          [['org.clojure/clojure "1.10.0"]]
    :plugins               [['lein-check-namespace-decls "LATEST"]]
    :source-paths          ["./"]}
   'lein-check-namespace-decls.test-project
   "1.0.0"
   (project-root test-dir)))

(defn- do-with-project [test-dir options f]
  (puget/with-options {:print-color true, :color-scheme {:string [:yellow]}}
    (println (puget/color-text :string (format "\n\nCheck %s" test-dir))))
  (testing (format "files in %s with options %s" (project-root test-dir) (pr-str options))
    (f (lein-project test-dir options))))

(defmacro ^:private with-project [[project-binding [test-dir options]] & body]
  `(do-with-project ~test-dir ~options (fn [~project-binding] ~@body)))

(deftest good-no-prefixes
  (with-project [project ["good-no-prefixes" {:prefix-rewriting false}]]
    (is (= nil
           (check-decls/check-namespace-decls project)))))

(deftest good-conditionals
  (with-project [project ["good-conditionals" {:prefix-rewriting false}]]
    (is (= nil
           (check-decls/check-namespace-decls project)))))

(deftest good-prefixes
  (with-project [project ["good-prefixes" {:prefix-rewriting true}]]
    (is (= nil
           (check-decls/check-namespace-decls project)))))

(deftest good-refer-all
  (with-project [project ["good-refer-all"]]
    (is (= nil
           (check-decls/check-namespace-decls project)))))

(deftest ignore-unused
  (with-project [project ["ignore-unused" {:prune-ns-form false}]]
    (is (= nil
           (check-decls/check-namespace-decls project)))))

(deftest bad-unsorted
  (with-project [project ["bad-unsorted"]]
    (is (thrown?
         clojure.lang.ExceptionInfo
         (check-decls/check-namespace-decls project)))))

(deftest bad-unused
  (with-project [project ["bad-unused"]]
    (is (thrown?
         clojure.lang.ExceptionInfo
         (check-decls/check-namespace-decls project)))))

(deftest bad-prefixes
  (with-project [project ["bad-prefixes"]]
    (is (thrown?
         clojure.lang.ExceptionInfo
         (check-decls/check-namespace-decls project)))))

(deftest invalid-file
  (with-project [project ["invalid-file"]]
    (is (thrown?
         clojure.lang.ExceptionInfo
         (check-decls/check-namespace-decls project)))))

(deftest ignore-paths
  (with-project [project ["invalid-file" {:ignore-paths [#"bad\.clj$"]}]]
    (is (= nil
           (check-decls/check-namespace-decls project)))))
