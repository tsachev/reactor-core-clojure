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
  ^{:doc    "A test-case for reactor-core.reducers."
    :author "Vladimir Tsanev"}
  reactor-core.reducer-test
  #?(:cljs (:require-macros [cljs.test :refer [async]]))
  #?(:clj
     (:use [reactor-core.test :only [async]]))
  (:require
    #?(:cljs [cljs.test :as t]
       :clj [clojure.test :as t])
            [reactor-core.reducers :refer []]
            [clojure.core.reducers :as r]
            [reactor-core.publisher :as p]))

#?(:cljs (set! *warn-on-infer* true)
   :clj  (set! *warn-on-reflection* true))

(t/deftest reducers-test
  (async done
    (let [reduced-flux (reduce + 0 (r/map inc (r/filter even? (p/range 1 9))))]
      (->> reduced-flux
           (p/subscribe (fn [value]
                          (t/is (= 24 value)))
                        (fn [e]
                          (t/is false e)
                          (done))
                        done)))))

(comment
  (reduce + 0 (range 1 5)))