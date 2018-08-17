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
  (:refer-clojure :exclude [concat delay empty filter map mapcat range reduce take])
  (:require
    [reactor-core.protocols :as p]
    [reactive-streams.js :refer [Publisher Subscriber]]
    ["reactor-core-js/flux" :refer [Flux DirectProcessor]]
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
  ([error-or-fn]
   (error error-or-fn false))
  ([error-or-fn when-requested?]
   (if when-requested?
     (.doOnNext (just nil) (fn [_] (throw (if (fn? error-or-fn) (error-or-fn) error-or-fn))))
     (.doOnSubscribe (empty) (fn [_] (throw (if (fn? error-or-fn) (error-or-fn) error-or-fn)))))))

(extend-protocol Publisher
  Flux
  (subscribe [flux subscriber]
    ;(println "subscribe" flux "with" subscriber)
    (.subscribe ^Flux flux subscriber)))

(defn from-dispatch-fn [x]
  (if (array? x)
    ::array
    (type x)))

(defmulti from "" ^Publisher from-dispatch-fn)

(defmethod from ::array [a] (.fromArray Flux a))

(extend-type Flux
  p/MapOperator
  (-map [flux transformer]
    (.map flux transformer))
  p/FlatMapOperator
  (-flat-map [flux mapper]
    (.flatMap flux mapper))
  (-concat-map [flux mapper]
    (.concatMap flux mapper))
  (-concat-map [flux mapper prefetch]
    (.concatMap flux mapper prefetch))
  p/FilterOperator
  (-filter [flux predicate] (.filter flux predicate))
  p/TakeOperator
  (-take [flux n] (.take flux ^long n))
  p/SkipOperator
  (-skip [flux n] (.skip flux ^long n))
  p/ZipOperator
  (-zip-with [flux other combinator] (.zipWith flux other combinator))
  p/HideOperator
  (-hide [flux] (.hide flux))
  p/TakeUntilOperator
  (-take-until [flux other] (.takeUntil flux other)))

(defn interval
  ([period]
   (interval period period))
  ([delay period]
   (.interval Flux delay period)))

(defn delay
  [delay]
  (.timer Flux delay))

(defn concat
  [sources]
  (.concat Flux sources))

(extend-type DirectProcessor
  Subscriber
  (onSubscribe [p s] (.onSubscribe p s))
  (onNext [p t] (.onNext p t))
  (onError [p t] (.onError p t))
  (onComplete [p] (.onComplete p)))

(defn mono-processor
  ([]
   (let [p (new DirectProcessor)]
     (specify! p
       Subscriber
       (onNext [_ item]
         (.onNext p item)
         (.onComplete p)))
     (reactive-streams.js/->ISubscriber p))))

(defn zip
  ([sources zipper]
   (cond
     (= 2 (count sources))
     (.zip2 Flux (first sources) (second sources) (fn [a1 a2]
                                                    (zipper a1 a2)))
     :else
     (.zip Flux (apply array sources) (fn [args]
                                        (zipper args)))))
  ([sources zipper prefetch]
   (.zip Flux (apply array sources) (fn [args]
                                      (zipper args)) prefetch)))


(defn range
  [start count]
  (.range Flux start count))

;; filtering
(defn filter
  [predicate publisher]
  (p/-filter publisher predicate))

(defn skip
  [skipped publisher]
  (p/-skip publisher skipped))

(defn take
  [n publisher]
  (p/-take publisher n))

;; transforming
(defn map
  [mapper publisher]
  (p/-map publisher mapper))

(defn mapcat
  ([mapper publisher]
   (p/-concat-map publisher mapper)))

(defn flat-map
  [mapper publisher]
  (p/-flat-map flux mapper))

(defn hide
  [publisher]
  (p/-hide publisher))

;; conditional
(defn take-until
  [other publisher]
  (p/-take-until publisher other))

;; aggregate
(defn reduce
  ([aggregator ^Flux flux]
   (reduce aggregator js/undefined flux))
  ([accumulator initial ^Flux flux]
   (.reduce flux (fn [] initial) accumulator)))

;(extend-type CallbackSubscriber
;  Subscriber
;  (onSubscribe [this s] (.onSubscribe this s))
;  (onNext [this t] (.onNext [this t]))
;  (onError [this ^js/Error t] (.onError this t))
;  (onComplete [this] (.onComplete this)))

(defn subscribe
  ([on-next ^Publisher p]
   (subscribe on-next (fn [_]) p))
  ([on-next on-error ^Publisher p]
   (subscribe on-next on-error (fn []) p))
  ([on-next on-error on-complete ^Publisher p]
   (cond
     ;(instance? Flux p)
     ;(.subscribe ^Flux p
     ;            (new CallbackSubscriber on-next on-error on-complete))
     :else
     (reactive-streams.js/subscribe p
                                    (new CallbackSubscriber on-next on-error on-complete)))))
