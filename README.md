[![Dependencies Status](https://versions.deps.co/camsaul/lein-check-namespace-decls/status.svg)](https://versions.deps.co/camsaul/lein-check-namespace-decls)
[![Circle CI](https://circleci.com/gh/camsaul/lein-check-namespace-decls.svg?style=svg)](https://circleci.com/gh/camsaul/lein-check-namespace-decls)
[![License](https://img.shields.io/badge/license-Eclipse%20Public%20License-blue.svg)](https://raw.githubusercontent.com/camsaul/lein-check-namespace-decls/master/LICENSE)

[![Clojars Project](https://clojars.org/lein-check-namespace-decls/latest-version.svg)](http://clojars.org/lein-check-namespace-decls)

# lein-check-namespace-decls

A Leiningen plugin that lints source files in your Clojure project to
check whether the `ns` declarations are cleaned the way
[`refactor-nrepl`](https://github.com/clojure-emacs/refactor-nrepl)
and
[`cljr-refactor`](https://github.com/clojure-emacs/clj-refactor.el)
would clean them (such as with the `M-x cljr-clean-ns` command in
Emacs).

Checks the following:

*  Namespaces and imports in the `ns` declaration are sorted
*  No unused namespaces are present (configurable)
*  `:require` forms are used instead of `:use`
*  The top-level forms inside the `ns` form follow the [conventional
   order](https://github.com/bbatsov/clojure-style-guide#comprehensive-ns-declaration):
   `:gen-class`, `:refer-clojure`, `:require`, `:import`
*  Namespaces in `:require` use shared prefixes (e.g. `(:require [clojure [string :as str] [data :as data]])`)

For files with `ns` declarations that don't satisfy these rules, the
linter tells you how to fix namespace declarations that aren't cleaned
properly.

## Example Usage

![Example usage screenshot](images/screenshot.png)

What happened? The linter detected an unused namespace,
`clojure.string`, and removed it; it sorted the namespaces as well.

For each `ns` declaration it finds that isn't "clean" (i.e. not
properly sorted or with unused namespaces), it will print a message
like the one above; if any unclean namespaces were found, the linter
will exit with a nonzero status.

## Usage

### Adding the dependency to `:plugins`

Add `[lein-check-namespace-decls "1.0.1"]` into the `:plugins` vector of your
`project.clj` or `~/.lein/profiles.clj`.

```clj
(defproject my-project
  :plugins [[lein-check-namespace-decls "1.0.1"]])
```

### Running the linter

Run the linter like this:

```
lein check-namespace-decls
```

This will check `ns` declaration forms for all Clojure source files in
the `:source-paths` of your project (usually `./src/`).

## Configuration

Add a `:check-namespace-decls` key to your `project.clj` to configure
the way `refactor-nrepl` cleans namespaces. All options are passed
directly to `refactor-nrepl`.

```clj
(defproject my-project
  :check-namespace-decls {:prefix-rewriting false})
```

The complete list of available options can be found
[here](https://github.com/clojure-emacs/refactor-nrepl/blob/master/src/refactor_nrepl/config.clj).
The most interesting ones when using this as a linter are:

###### `:prefix-rewriting`

By setting `{:prefix-rewriting false}` the `ns` form will not be
expected to use prefix lists when including several libs whose names
have the same prefix. The default behavior uses prefixes:

```clj
;; Cleaned ns form with :prefix-rewriting true (default)
(ns my-project.core
  (:require [clojure.tools.namespace
             [find :as ns.find]
             [parse :as ns.parse]]))
```

Contrast this to `ns` forms without prefixes:

```clj
;; Cleaned ns form with :prefix-rewriting false
(ns my-project.core
  (:require [clojure.tools.namespace.find :as ns.find]
            [clojure.tools.namespace.parse :as ns.parse]))
```

Opinions on whether or not to use prefixes in `:require` forms vary in
the Clojure community; I ([@camsaul](https://github.com/camsaul))
personally like them, so I stuck with the default behavior of
`refactor-nrepl`, which is to use them.

###### `:ignore-paths`

Specify a collection of regexes of filenames to ignore.

```clj
;; ignore any files ending in core.clj
(defproject my-project
  :check-namespace-decls {:ignore-paths [#".*core\.clj$"]})
```

###### `:prune-ns-form`

Whether to remove unused namespaces from the `ns` form. Defaults to
`true`; if you don't want unused namespaces to be considered an error,
set this to `false`.


#### Including or excluding source directories

`lein-check-namespace-decls` simply checks any Clojure source files it finds in `:source-paths`, excluding any that match a pattern in `:ignore-paths`.

To include or exclude entire directories from the linter you can
modify or `^:replace` `:source-paths`; the easiest way to do this without
affecting the rest of your project is to add a profile for
`lein-check-namespace-decls` and an alias that automatically applies
it:

```clj
;; Check the sources under `./test/` in addition to the normal :source-paths (e.g. `./src/`)
(defproject my-project
  :aliases {"check-namespace-decls" ["with-profile" "+check-namespace-decls" "check-namespace-decls"]}
  :plugins [[lein-check-namespace-decls "1.0.1"]]
  :profiles {:check-namespace-decls {:source-paths ["test"]}})
```

If the above seems like black magic, the [Leiningen
documentation](https://github.com/technomancy/leiningen/blob/master/doc/PROFILES.md) does a great job of explaining how Leiningen plugins work in more detail.

#### Verbose logging

Like Leiningen itself, setting the environment variable `DEBUG=true`
will add additional log messages to linter output -- specifically, it
will log messages about each file it checks and files that are skipped
due to patterns in `:ignore-paths` (and the patterns that matched
those files). This is useful for debugging which files are checked by the linter.

## License

Copyright © 2019 Cam Saul.

Distributed under the Eclipse Public License, same as Clojure.
