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
  (:refer-clojure :exclude [concat empty filter map range reduce take take-while])
  (:require
    [reactor-core.util :as u])
  (:import
    (reactor.core.publisher Flux Mono)
    (org.reactivestreams Subscriber Publisher)
    (java.time Duration)))

(defn mono?
  [p]
  (instance? Mono p))

(defn flux?
  [p]
  (instance? Flux p))

(defn never
  []
  (Flux/never))

(defn empty
  []
  (Flux/empty))

(defn just
  [data]
  (Mono/just data))

(defn error
  [error]
  (Mono/error error))

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
  (.filter flux (u/as-predicate p)))

(defn skip
  [^Flux flux ^long skipped]
  (.skip flux skipped))

(defn take
  [^Flux flux ^long n]
  (.take flux n))

(defn take-while
  [^Flux flux p]
  (.takeWhile flux (u/as-predicate p)))

(defn map
  [^Flux flux mapper]
  (.map flux (u/as-function mapper)))

(defn flat-map
  [^Flux flux mapper]
  (.flatMap flux (u/as-function mapper)))

(defn flat-map-sequential
  [^Flux flux mapper]
  (.flatMapSequential flux (u/as-function mapper)))

(defn hide
  [^Flux flux]
  (.hide flux))

(defn concat-map
  [^Flux flux mapper]
  (.concatMap flux (u/as-function mapper)))

(defn reduce
  ([^Flux flux aggregator]
   (.reduce flux (u/as-bi-function aggregator)))
  ([^Flux flux initial accumulator]
   (.reduce flux initial (u/as-bi-function accumulator))))

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
     (mono? p)
     (.subscribe ^Mono p
                 (u/as-consumer on-next)
                 (u/as-consumer on-error)
                 (u/as-runnable on-complete)
                 (u/as-consumer on-subscribe))
     (flux? p)
     (.subscribe ^Flux p
                 (u/as-consumer on-next)
                 (u/as-consumer on-error)
                 (u/as-runnable on-complete)
                 (u/as-consumer on-subscribe))
     :else
     (subscribe-with p (reify Subscriber
                         (onNext [_ n] (on-next n))
                         (onError [_ e] (on-error e))
                         (onComplete [_] (on-complete))
                         (onSubscribe [_ s] (on-subscribe s)))))))

(extend-protocol clojure.core.protocols/CollReduce
  Flux
  (coll-reduce
    ([this f]
     (reduce this f))
    ([this f start]
     (reduce this start f))))

