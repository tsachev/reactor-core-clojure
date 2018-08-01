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
  (:refer-clojure :exclude [concat empty filter map mapcat range reduce take])
  (:require
    [reactor-core.protocols :as p :refer [Fluxable to-flux]]
    [reactor-core.reactive-streams :refer [IPublisher]]
    ["reactor-core-js/flux" :refer [Flux Mono]]
    ["reactor-core-js/subscriber" :refer [CallbackSubscriber]]))

(set! *warn-on-infer* true)

(defn just
  [data]
  (.just Flux data))

(defn empty
  []
  (.empty Flux))

(defn never
  []
  (.never Flux))

(defn error
  [error]
  (.fromCallable Flux (fn [] (throw error))))

(extend-protocol p/Fluxable
  Flux
  (to-flux [flux] flux)
  IPublisher
  (to-flux [source] (.from Flux source)))

(defn flux
  [source]
  (cond
    (satisfies? Fluxable source) (p/to-flux source)
    (array? source) (.fromArray Flux source)
    (nil? source) (empty)
    :else (.just Flux source)))

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

(defn mapcat
  [^Flux flux mapper]
  (.concatMap flux mapper))

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

