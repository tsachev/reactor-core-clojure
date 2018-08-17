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
  ^{:doc    "A test-case for map operator."
    :author "Vladimir Tsanev"}
  reactor-core.publisher.map-test
  #?(:cljs (:require-macros [cljs.test :refer [async]])
     :clj
           (:use [reactor-core.test :only [async]]))
  (:require
    #?(:cljs [cljs.test :as t]
       :clj [clojure.test :as t])
            [reactor-core.publisher :as reactor]))

(t/deftest normal
  (async done
    (let [values (atom [])]
      (->> (reactor/just 1)
           (reactor/map inc)
           (reactor/subscribe
             (fn [value]
               (swap! values conj value))
             (fn [_]
               (t/is false))
             (fn []
               (t/is (= @values [2]))
               (done)))))))

(t/deftest throws
  (async done
    (->> (reactor/just 1)
         (reactor/map (fn [_] (throw (ex-info "forced failure" {}))))
         (reactor/subscribe
           (fn [_]
             (t/is false))
           (fn [e]
             (t/is (thrown-with-msg? #?(:clj Exception :cljs js/Error) #"forced failure" (throw e)))
             (done))
           (fn []
             (t/is false))))))

(t/deftest hidden-map-filter
  (async done
    (let [values (atom [])]
      (->> (reactor/just 1)
           (reactor/hide)
           (reactor/map inc)
           (reactor/filter #(zero? (bit-and % 1)))
           (reactor/subscribe
             (fn [value]
               (swap! values conj value))
             (fn [_]
               (t/is false))
             (fn []
               (t/is (= @values [2]))
               (done)))))))