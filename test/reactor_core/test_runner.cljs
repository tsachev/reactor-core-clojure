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

(ns
  ^{:doc    "Run cljs tests."
    :author "Vladimir Tsanev"}
  reactor-core.test-runner
  (:require
    [cljs.test :refer-macros [run-tests]]
    [reactor-core.processor-test]
    [reactor-core.publisher-test]
    [reactor-core.publisher.map-test]
    [reactor-core.publisher.mapcat-test]
    [reactor-core.publisher.take-test]
    [reactor-core.publisher.take-until-test]
    [reactor-core.publisher.zip-test]
    [reactor-core.reducer-test]))

(enable-console-print!)

(run-tests 'reactor-core.publisher-test
           ;; operators
           'reactor-core.publisher.map-test
           'reactor-core.publisher.mapcat-test
           'reactor-core.publisher.take-test
           'reactor-core.publisher.take-until-test
           'reactor-core.publisher.zip-test
           ;; processor
           'reactor-core.processor-test
           ;; clojure integration
           'reactor-core.reducer-test)
