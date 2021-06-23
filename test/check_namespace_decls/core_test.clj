(ns check-namespace-decls.core-test
  (:require [check-namespace-decls.core :as check-decls.core]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [leiningen.core.project :as lein.project]
            [puget.printer :as puget]))

(defn- project-root [test-dir]
  (let [file (io/file (str "test-namespaces/" test-dir))]
    (.getAbsolutePath file)))

(defn- lein-project [test-dir options]
  (lein.project/absolutize-paths
   (lein.project/make
    {:check-namespace-decls options
     :dependencies          [['org.clojure/clojure "1.10.0"]]
     :plugins               [['lein-check-namespace-decls "LATEST"]]
     :source-paths          ["./"]}
    'lein-check-namespace-decls.test-project
    "1.0.0"
    (project-root test-dir))))

(defn- do-with-project [test-dir options f]
  (puget/with-options {:print-color true, :color-scheme {:string [:yellow]}}
    (println (puget/color-text :string (format "\n\nCheck %s" test-dir))))
  (testing (format "files in %s with options %s" (project-root test-dir) (pr-str options))
    (let [project (lein-project test-dir options)]
      (testing (format "\nsource paths = %s\noptions = %s"
                       (pr-str (:source-paths project))
                       (pr-str (:check-namespace-decls project)))
        (f project)))))

(defmacro ^:private with-project [[project-binding [test-dir options]] & body]
  `(do-with-project ~test-dir ~options (fn [~project-binding] ~@body)))

(defn- check-decls [{:keys [source-paths], options :check-namespace-decls}]
  (check-decls.core/errors source-paths options))

(defn- capture-output [thunk]
  (binding [puget/*options* (dissoc puget/*options* :color-scheme :color-markup)]
    (str/split-lines (str/trim (with-out-str (thunk))))))

(deftest good-no-prefixes
  (with-project [project ["good_no_prefixes" {:prefix-rewriting false}]]
    (is (nil? (check-decls project)))
    (is (= ["Checked 1 files; 0 had errors."]
           (capture-output #(check-decls project))))))

(deftest good-conditionals
  (with-project [project ["good_conditionals" {:prefix-rewriting false}]]
    (is (nil? (check-decls project)))
    (is (= ["Checked 2 files; 0 had errors."]
           (capture-output #(check-decls project))))))

(deftest good-prefixes
  (with-project [project ["good_prefixes" {:prefix-rewriting true}]]
    (is (nil? (check-decls project)))
    (is (= ["Checked 1 files; 0 had errors."]
           (capture-output #(check-decls project))))))

(deftest good-refer-all
  (with-project [project ["good_refer_all"]]
    (is (nil? (check-decls project)))
    (is (= ["Checked 2 files; 0 had errors."]
           (capture-output #(check-decls project))))))

(deftest ignore-unused
  (with-project [project ["ignore_unused" {:prune-ns-form false}]]
    (is (nil? (check-decls project)))
    (is (= ["Checked 0 files; 0 had errors."]
           (capture-output #(check-decls project))))))

(deftest bad-unsorted
  (with-project [project ["bad_unsorted"]]
    (is (= {(str (project-root "bad_unsorted") "/core.clj")
            {:expected '(ns bad-unsorted.core (:require [clojure [data :as data] [string :as str]]))
             :actual   '(ns bad-unsorted.core (:require [clojure [string :as str] [data :as data]]))}}
           (check-decls project)))
    (is (= ["Checked 1 files; 1 had errors."]
           (capture-output #(check-decls project))))))

(deftest bad-unused
  (with-project [project ["bad_unused"]]
    (is (= {(str (project-root "bad_unused") "/unused/core.clj")
            {:expected '(ns bad-unused.unused.core
                          (:require
                           [clojure.data :as data]
                           [clojure.java.io :as io]))
             :actual   '(ns bad-unused.unused.core
                          (:require
                           [clojure
                            [data :as data]
                            [string :as str]]
                           [clojure.java.io :as io]))}}
           (check-decls project)))
    (is (= ["Checked 1 files; 1 had errors."]
           (capture-output #(check-decls project))))))

(deftest bad-prefixes
  (with-project [project ["bad_prefixes"]]
    (is (= {(str (project-root "bad_prefixes") "/core.clj")
            {:expected
             '(ns bad-prefixes.core (:require [clojure [data :as data] [string :as str]]))
             :actual
             '(ns bad-prefixes.core (:require [clojure.data :as data] [clojure.string :as str]))}}
           (check-decls project)))
    (is (= ["Checked 1 files; 1 had errors."]
           (capture-output #(check-decls project))))))

(deftest invalid-file
  (with-project [project ["invalid_file"]]
    (let [filename (str (project-root "invalid_file") "/bad.clj")
          result (check-decls project)]
      (testing (str "\nresult = " (pr-str result))
        (is (map? result))
        (is (= [filename] (keys result)))
        (let [file-result (get result filename)]
          (is (map? file-result))
          (is (= [:error] (keys file-result)))
          (let [e (get file-result :error)]
            (is (instance? clojure.lang.Compiler$CompilerException e))
            (is (str/includes? (ex-message e) "Syntax error reading source"))))))
    (is (= ["Checked 2 files; 1 had errors."]
           (capture-output #(check-decls project))))))

(deftest ignore-paths
  (with-project [project ["invalid_file" {:ignore-paths [#"bad\.clj$"]}]]
    (is (nil? (check-decls project)))
    (is (= ["Checked 1 files; 0 had errors."]
           (capture-output #(check-decls project))))))
