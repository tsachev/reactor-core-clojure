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
  ^{:doc    "A test-case for reactor-core.publisher."
    :author "Vladimir Tsanev"}
  reactor-core.publisher-test
  #?(:cljs (:require-macros [cljs.test :refer [async]])
     :clj
           (:use [reactor-core.test :only [async]]))
  (:require
    #?(:cljs [cljs.test :as t]
       :clj [clojure.test :as t])
            [reactor-core.publisher :as p]))

#?(:cljs (set! *warn-on-infer* true)
   :clj  (set! *warn-on-reflection* true))

(t/deftest range-test
  (t/testing "consume range."
    (async done
      (let [values (atom [])]
        (->> (p/range 1 10)
             (p/subscribe
               (fn [value]
                 (swap! values conj value))
               (fn [_])
               (fn []
                 (t/is (= @values [1 2 3 4 5 6 7 8 9 10]))
                 (done))))))))

(t/deftest error-test
  (t/testing "consume error"
    (async done
      (->> (p/error (ex-info "test" {}))
           (p/subscribe
             (fn [_] (t/is false))
             (fn [e]
               (t/is (thrown-with-msg? #?(:clj Exception :cljs js/Error) #"test" (throw e)))
               (done))
             (fn [] (t/is false)))))))

(t/deftest empty-test
  (t/testing "consume empty"
    (async done
      (->> (p/empty)
           (p/subscribe
             (fn [_] (t/is false))
             (fn [_] (t/is false))
             (fn [] (t/is true)
               (done)))))))

(t/deftest array-test
  (t/testing "consume array."
    (async done
      (let [values (atom [])]
        (->> (p/from (into-array [1 2 3]))
             (p/subscribe
               (fn [value]
                 (swap! values conj value))
               (fn [_]
                 (t/is false))
               (fn []
                 (t/is (= @values [1 2 3]))
                 (done))))))))

(t/deftest even-numbers-test
  (t/testing "finishes"
    (async done
      (let [even-numbers (p/filter even? (p/range 1 6))
            noop (fn [_])]
        (p/subscribe noop noop done even-numbers))))

  (t/testing "has 3 numbers"
    (async done
      (let [cnt (atom 0)
            even-numbers (p/filter even? (p/range 1 6))
            on-number (fn [_] (reset! cnt (inc @cnt)))]
        (p/subscribe on-number
                     (constantly nil)
                     (fn []
                       (t/is (= 3 @cnt) "got 3 numbers")
                       (done))
                     even-numbers))))
  (t/testing "has no errors"
    (async done
      (let [even-numbers (->> (p/range 1 6)
                              (p/filter even?)
                              (p/map (fn [_]
                                       (throw (ex-info "oh no" {})))))]
        (p/subscribe (fn [_])
                     (fn [e]
                       (t/is (thrown-with-msg? #?(:clj Exception :cljs js/Error) #"oh no" (throw e)))
                       (done))
                     (fn []
                       (t/is false "on-complete")
                       (done))
                     even-numbers)))))
