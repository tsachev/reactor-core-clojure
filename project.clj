;
; Copyright 2018 the original author or authors.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;      http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;

(defproject io.tsachev/reactor-core-clojure "0.1.0-SNAPSHOT"
  :description "A Clojure(Script) for for Reactor Flux and Mono"
  :url "https://github.com/tsachev/reactor-core-clojure"
  :license {:name         "The Apache Software License, Version 2.0"
            :url          "https://www.apache.org/licenses/LICENSE-2.0.txt"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [io.projectreactor/reactor-core "3.1.8.RELEASE"]]
  :profiles {:cljs {:dependencies [[org.clojure/clojurescript "1.10.339" :scope "provided"]]
                    :plugins      [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]
                    :cljsbuild    {:test-commands {"publisher" ["node" "target/publisher-tests.js"]}
                                   :builds        {:test {:source-paths   ["src" "test"]
                                                          :notify-command ["node" "target/publisher-tests.js"]
                                                          :compiler       {:npm-deps      {:reactor-core-js "0.5.0"}
                                                                           :install-deps  true
                                                                           :output-to     "target/publisher-tests.js"
                                                                           :optimizations :none
                                                                           :target        :nodejs
                                                                           :main          reactor-core.publisher-test}}}}}
             :dev  {:dependencies [[com.fzakaria/slf4j-timbre "0.3.12"]]
                    :plugins      [[lein-cloverage "1.0.11"]]}})
