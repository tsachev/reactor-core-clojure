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
  ^{:doc    "Converters from clojure functions to various java SAM interfaces."
    :author "Vladimir Tsanev"}
  reactor-core.util.function
  (:import
    (java.util.function Consumer BiFunction Predicate Function LongFunction BiPredicate LongConsumer Supplier BooleanSupplier BiConsumer)))

(defn as-predicate
  [p]
  (if (or (nil? p) (instance? Predicate p)) p (reify Predicate (test [_ t] (p t)))))

(defn as-bi-predicate
  [p]
  (if (or (nil? p) (instance? BiPredicate p)) p (reify BiPredicate (test [_ t u] (p t u)))))

(defn as-function
  [f]
  (if (or (nil? f) (instance? Function f)) f (reify Function (apply [_ t] (f t)))))

(defn as-long-function
  [f]
  (if (or (nil? f) (instance? LongFunction f)) f (reify LongFunction (apply [_ value] (f value)))))

(defn ^BiFunction as-bi-function
  [f]
  (if (or (nil? f) (instance? BiFunction f)) f (reify BiFunction (apply [_ t u] (f t u)))))

(defn as-consumer
  [c]
  (if (or (nil? c) (instance? Consumer c)) c (reify Consumer (accept [_ t] (c t)))))

(defn as-long-consumer
  [c]
  (if (or (nil? c) (instance? LongConsumer c)) c (reify LongConsumer (accept [_ value] (c value)))))

(defn as-bi-consumer
  [c]
  (if (or (nil? c) (instance? BiConsumer c)) c (reify BiConsumer (accept [_ t u] (c t u)))))

(defn as-runnable
  [r]
  (if (or (nil? r) (instance? Runnable r)) r (reify Runnable (run [_] (r)))))

(defn as-callable
  [c]
  (if (or (nil? c) (instance? Callable c)) c (reify Callable (call [_] (c)))))

(defn as-supplier
  [s]
  (if (or (nil? s) (instance? Supplier s)) s (reify Supplier (get [_] (s)))))

(defn as-boolean-supplier
  [s]
  (if (or (nil? s) (instance? BooleanSupplier s)) s (reify BooleanSupplier (getAsBoolean [_] (s)))))
