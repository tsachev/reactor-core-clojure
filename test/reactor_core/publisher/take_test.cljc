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
  ^{:doc    "A test-case for take operator."
    :author "Vladimir Tsanev"}
  reactor-core.publisher.take-test
  #?(:cljs (:require-macros [cljs.test :refer [async]])
     :clj  (:use [reactor-core.test :only [async]]))
  (:require
    #?(:cljs [cljs.test :as t]
       :clj [clojure.test :as t])
            [reactor-core.publisher :as reactor]))

(t/deftest normal
  (async done
    (let [values (atom [])]
      (->> (reactor/range 1 10)
           (reactor/take 5)
           (reactor/subscribe
             (fn [value] (swap! values conj value))
             (fn [_] (t/is false))
             (fn []
               (t/is (= @values [1 2 3 4 5]))
               (done)))))))

(t/deftest take-zero
  (async done
    (->> (reactor/range 1 10)
         (reactor/take 0)
         (reactor/subscribe
           (fn [_] (t/is false))
           (fn [_] (t/is false))
           (fn []
             (t/is true)
             (done))))))