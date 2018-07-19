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
  ^{:doc    ""
    :author "Vladimir Tsanev"}
  reactor-core.util
  (:import
    (java.util.function Consumer BiFunction Predicate Function)))

(defn noop
  [& _])

(defn as-predicate
  [p]
  (if (or (nil? p) (instance? Predicate p)) p (reify Predicate (test [_ t] (p t)))))

(defn as-function
  [f]
  (if (or (nil? f) (instance? Function f)) f (reify Function (apply [_ t] (f t)))))

(defn as-bi-function
  [f]
  (if (or (nil? f) (instance? BiFunction f)) f (reify BiFunction (apply [_ t u] (f t u)))))

(defn as-consumer
  [c]
  (if (or (nil? c) (instance? Consumer c)) c (reify Consumer (accept [_ t] (c t)))))

(defn as-runnable
  [r]
  (if (or (nil? r) (instance? Runnable r)) r (reify Runnable (run [_] (r)))))

(defn as-callable
  [c]
  (if (or (nil? c) (instance? Callable c)) c (reify Callable (call [_] (c)))))
