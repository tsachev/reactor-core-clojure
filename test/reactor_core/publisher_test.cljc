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

(ns reactor-core.publisher-test
  #?(:cljs (:require-macros [cljs.test :refer [async]])
     :clj
           (:use [reactor-core.test :only [async]]))
  (:require #?(:cljs [cljs.test :as t]
               :clj [clojure.test :as t])
                    [clojure.core.reducers :as r]
                    [reactor-core.publisher :as p]))

#?(:cljs (enable-console-print!))

(t/deftest range-test
  (t/testing "consume range."
    (async done
      (let [cnt (atom [])]
        (-> (p/range 1 10)
            (p/subscribe
              #(reset! cnt (conj @cnt %))
              (fn [_])
              (fn []
                (t/is (= @cnt [1 2 3 4 5 6 7 8 9 10]))
                (done))))))))

(t/deftest reducers-test
  (async done
    (let [reduced-flux (reduce + 0 (r/map inc (r/filter even? (p/range 1 9))))]
      (-> reduced-flux
          (p/subscribe #(t/is (= 24 %))
                       (fn [e]
                         (t/is false e)
                         (done))
                       done)))))

(t/deftest even-numbers-test
  (t/testing "finishes"
    (async done
      (let [even-numbers (p/filter (p/range 1 6) even?)
            noop (fn [_])]
        (p/subscribe even-numbers noop noop done))))

  (t/testing "has 3 numbers"
    (async done
      (let [cnt (atom 0)
            even-numbers (p/filter (p/range 1 6) even?)
            on-number (fn [_] (reset! cnt (inc @cnt)))]
        (p/subscribe even-numbers
                     on-number
                     (constantly nil)
                     (fn []
                       (t/is (= 3 @cnt) "got 3 numbers")
                       (done))))))
  (t/testing "has no errors"
    (async done
      (let [even-numbers (-> (p/range 1 6)
                             (p/filter even?)
                             (p/map (fn [_]
                                      (throw (ex-info "oh no" {})))))]
        (p/subscribe even-numbers
                     (fn [_])
                     (fn [e]
                       (t/is (thrown-with-msg? #?(:clj Exception :cljs js/Error) #"oh no" (throw e)))
                       (done))
                     (fn []
                       (t/is false "on-complete")
                       (done)))))))

(comment
  (defmacro subscribe [s & body])


  (t/deftest use-test-subscribe
    (subscribe s
               (p/subscribe-with (p/range 1 10) s)
               (t/is (= [1 2 3 4 5 6 7 8 9 10] (values s)))
               (t/is (empty? (errors s)))
               (t/is (complete? s)))))

#?(:cljs (t/run-tests 'reactor-core.publisher-test))
