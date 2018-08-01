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
  (:refer-clojure :exclude [concat empty filter map mapcat range reduce take take-while])
  (:require
    [reactor-core.util.core :refer [array?]]
    [reactor-core.util.function :as f]
    [reactor-core.protocols :as p :refer [Fluxable Monoable to-flux to-mono]])
  (:import
    (reactor.core.publisher Flux Mono)
    (org.reactivestreams Subscriber Publisher)
    (java.time Duration)
    (java.util.stream Stream)
    (clojure.lang IFn)))

(set! *warn-on-reflection* true)

(defn just
  [data]
  (Mono/just data))

(defn empty
  []
  (Flux/empty))

(defn never
  []
  (Flux/never))

(defn error
  [error]
  (Mono/error error))

(extend-protocol p/Monoable
  Mono
  (to-mono [mono] mono)
  Publisher
  (to-mono [source] (Mono/fromDirect source))
  IFn
  (to-mono [f] (Mono/fromSupplier (f/as-supplier f)))
  Callable
  (to-mono [c] (Mono/fromCallable c)))

(defn mono
  [source]
  (cond
    (satisfies? p/Monoable source) (p/to-mono source)
    (nil? source) (Mono/empty)
    :else
    (Mono/justOrEmpty source)))

(extend-protocol p/Fluxable
  Flux
  (to-flux [flux] flux)
  Mono
  (to-flux [mono] (.flux mono))
  Publisher
  (to-flux [source] (Flux/from source))
  Iterable
  (to-flux [it] (Flux/fromIterable it))
  Stream
  (to-flux [s] (Flux/fromStream s)))

(defn flux
  [source]
  (cond
    (satisfies? Fluxable source) (p/to-flux source)
    (array? source) (Flux/fromArray source)
    (nil? source) (empty)
    :else (Flux/just source)))

(defn interval
  [delay period]
  (Flux/interval ^Duration delay ^Duration period))

(defn concat
  [sources]
  (Flux/concat ^Iterable sources))

(defn range
  [start count]
  (Flux/range start count))

(defn filter
  [^Flux flux p]
  (.filter flux (f/as-predicate p)))

(defn skip
  [^Flux flux ^long skipped]
  (.skip flux skipped))

(defn take
  [^Flux flux ^long n]
  (.take flux n))

(defn take-while
  [^Flux flux p]
  (.takeWhile flux (f/as-predicate p)))

(defn map
  [p mapper]
  (.map (to-flux p) (f/as-function mapper)))

(defn mapcat
  ([^Flux flux mapper]
   (.concatMap flux (f/as-function mapper)))
  ([^Flux flux mapper prefetch]
   (.concatMap flux (f/as-function mapper) prefetch)))

(defn flat-map
  [^Flux flux mapper]
  (.flatMap flux (f/as-function mapper)))

(defn flat-map-sequential
  [^Flux flux mapper]
  (.flatMapSequential flux (f/as-function mapper)))

(defn hide
  [^Flux flux]
  (.hide flux))

(defn concat-map
  [^Flux flux mapper]
  (.concatMap flux (f/as-function mapper)))

(defn reduce
  ([^Flux flux aggregator]
   (.reduce flux (f/as-bi-function aggregator)))
  ([^Flux flux initial accumulator]
   (.reduce flux initial (f/as-bi-function accumulator))))

(defn subscribe-with
  [^Publisher p ^Subscriber s]
  (.subscribe p s)
  s)

(defn subscribe
  ([^Publisher p on-next]
   (subscribe p on-next nil))
  ([^Publisher p on-next on-error]
   (subscribe p on-next on-error nil))
  ([^Publisher p on-next on-error on-complete]
   (subscribe p on-next on-error on-complete nil))
  ([^Publisher p on-next on-error on-complete on-subscribe]
   (cond
     (instance? Mono p)
     (.subscribe ^Mono p
                 (f/as-consumer on-next)
                 (f/as-consumer on-error)
                 (f/as-runnable on-complete)
                 (f/as-consumer on-subscribe))
     (instance? Flux p)
     (.subscribe ^Flux p
                 (f/as-consumer on-next)
                 (f/as-consumer on-error)
                 (f/as-runnable on-complete)
                 (f/as-consumer on-subscribe))
     :else
     (subscribe-with p (reify Subscriber
                         (onNext [_ n] (on-next n))
                         (onError [_ e] (on-error e))
                         (onComplete [_] (on-complete))
                         (onSubscribe [_ s] (on-subscribe s)))))))



