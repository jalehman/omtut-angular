(defproject omtut-angular "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.reader "0.8.2"]
                 ;; CLJ
                 [ring/ring-core "1.2.0"]
                 [compojure "1.1.6"]
                 [cheshire "5.2.0"]
                 ;; CLJS
                 [org.clojure/clojurescript "0.0-2173"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [cljs-http "0.1.9"]
                 [secretary "1.1.0"]
                 [sablono "0.2.14"]
                 [om "0.5.3"]
                 [com.facebook/react "0.9.0.1"]
                 ;; Testing
                 [prismatic/dommy "0.1.2"]]

  :plugins [[lein-cljsbuild "1.0.2"]
            [lein-ring "0.8.7"]
            [lein-pdo "0.1.1"]
            ;; Testing
            [com.cemerick/clojurescript.test "0.3.0"]]

  :aliases {"dev" ["pdo" "cljsbuild" "auto" "dev," "ring" "server-headless"]
            "auto-test" ["cljsbuild" "auto" "test"]}

  :ring {:handler omtut-angular.core/app
         :init    omtut-angular.core/init}

  :source-paths ["src/clj"]

  :cljsbuild {:test-commands {"unit-tests" ["phantomjs" :runner
                                            "window.literal_js_executed=true"
                                            "test-cljs/vendor/es5-shim.js"
                                            "test-cljs/vendor/es5-sham.js"
                                            "test-cljs/vendor/console-polyfill.js"
                                            "resources/private/js/unit-test.js"]}

              :builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-to "resources/public/js/omtut_angular.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :source-map true
                                   :externs ["react/externs/react.js"]}}

                       {:id "test"
                        :source-paths ["src/cljs" "test-cljs"]

                        :notify-command ["phantomjs" :cljs.test/runner
                                         "window.literal_js_executed=true"
                                         "test-cljs/vendor/es5-shim.js"
                                         "test-cljs/vendor/es5-sham.js"
                                         "test-cljs/vendor/console-polyfill.js"
                                         "resources/private/js/unit-test.js"]

                        :compiler {:pretty-print true
                                   :output-dir "resources/private/js/"
                                   :output-to "resources/private/js/unit-test.js"
                                   :preamble ["react/react_with_addons.js"]
                                   :externs ["react/externs/react.js"]
                                   :optimizations :whitespace}}

                       {:id "release"
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-to "resources/public/js/omtut_angular.js"
                                   :source-map "resources/public/js/omtut_angular.js.map"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :output-wrapper false
                                   :preamble ["react/react.min.js"]
                                   :externs ["react/externs/react.js"]
                                   :closure-warnings
                                   {:non-standard-jsdoc :off}}}]})
