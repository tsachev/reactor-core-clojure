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
  reactor-core.publisher
  (:refer-clojure :exclude [concat empty filter map range reduce take])
  (:require
    ["reactor-core-js/reactivestreams-spec" :refer [Publisher]]
    ["reactor-core-js/flux" :refer [Flux Mono]]
    ["reactor-core-js/subscriber" :refer [CallbackSubscriber]]))

(defn mono?
  [p]
  (instance? Mono p))

(defn flux?
  [p]
  (instance? Flux p))

(defn never
  []
  (.never Flux))

(defn empty
  []
  (.empty Flux))

(defn just
  [data]
  (.just Flux data))

(defn error
  [error]
  (.fromCallable Flux (fn [] (throw error))))

(defn interval
  [delay period]
  (.interval Flux delay period))

(defn concat
  [sources]
  (.concat Flux sources))

(defn range
  [start count]
  (.range Flux start count))

(defn filter
  [^Flux flux p]
  (.filter flux p))

(defn skip
  [^Flux flux ^long skipped]
  (.skip flux skipped))

(defn take
  [^Flux flux ^long n]
  (.take flux n))

(defn map
  [^Flux flux mapper]
  (.map flux mapper))

(defn flat-map
  [^Flux flux mapper]
  (.flatMap flux mapper))

(defn hide
  [^Flux flux]
  (.hide flux))

(defn concat-map
  [^Flux flux mapper]
  (.concatMap flux mapper))

(defn reduce
  ([^Flux flux aggregator]
   (reduce flux js/undefined aggregator))
  ([^Flux flux initial accumulator]
   (.reduce flux (fn [] initial) accumulator)))

(defn subscribe
  ([^Publisher p on-next]
   (subscribe p on-next (fn [_])))
  ([^Publisher p on-next on-error]
   (subscribe p on-next on-error (fn [])))
  ([^Publisher p on-next on-error on-complete]
   (.subscribe p (new CallbackSubscriber on-next on-error on-complete))))

(extend-protocol IReduce
  Flux
  (-reduce
    ([this f]
     (reduce this f))
    ([this f start]
     (reduce this start f))))
