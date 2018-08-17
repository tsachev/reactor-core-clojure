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
  (:refer-clojure :exclude [concat delay empty filter map mapcat range reduce take take-while])
  (:require
    [reactor-core.protocols :as p]
    [reactive-streams.jvm]
    [reactor-core.util.core :refer [array?]]
    [reactor-core.util.function :as f])
  (:import
    (reactor.core.publisher Flux Mono MonoProcessor)
    (org.reactivestreams Subscriber Publisher)
    (java.time Duration Instant)
    (java.util.stream Stream)
    (java.util.concurrent CompletionStage TimeUnit)))

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
  ([error-or-fn]
   (Mono/error (if (fn? error-or-fn) (error-or-fn) error-or-fn)))
  ([error-or-fn when-requested?]
   (Flux/error (if (fn? error-or-fn) (error-or-fn) error-or-fn) when-requested?)))

(defn from-dispatch-fn [x]
  (if (array? x)
    ::array
    (type x)))

(derive Iterable ::iterable)
(derive Stream ::stream)
(derive CompletionStage ::promise)
(derive Publisher ::publisher)
(derive ::publisher ::mono)
(derive ::publisher ::flux)
(derive Mono ::mono)
(derive Flux ::flux)

(defmulti from "" ^Publisher from-dispatch-fn)
(defmethod from ::iterable [i] (Flux/fromIterable i))
(defmethod from ::stream [s] (Flux/fromStream ^Stream s))
(defmethod from ::array [a] (Flux/fromArray a))
(defmethod from ::promise [f] (Mono/fromCompletionStage f))
(defmethod from ::publisher [p] (Flux/from p))
(defmethod from ::mono [m] m)
(defmethod from ::flux [f] f)

(defmethod p/-zip [::mono 2] ([monos & [combinator]]
                               (Mono/zip (f/as-function combinator)
                                         ^"[Lreactor.core.publisher.Mono;" (to-array monos))))

;; cannot use extend-type because of https://dev.clojure.org/jira/browse/CLJ-825
(extend Flux
  p/FlatMapOperator
  {:-flat-map
   (fn [^Flux flux mapper]
     (.flatMap flux (f/as-function mapper)))
   :-concat-map
   (fn
     ([^Flux flux mapper] (.concatMap flux (f/as-function mapper)))
     ([^Flux flux mapper prefetch] (.concatMap flux (f/as-function mapper) prefetch)))})

(extend-type Flux
  p/MapOperator
  (-map [flux transformer]
    (.map flux (f/as-function transformer)))
  p/FilterOperator
  (-filter [flux predicate] (.filter flux (f/as-predicate predicate)))
  p/TakeOperator
  (-take [flux n] (.take flux ^long n))
  p/SkipOperator
  (-skip [flux n] (.skip flux ^long n))
  p/ZipOperator
  (-zip-with [flux other combinator] (.zipWith flux ^Publisher other (f/as-bi-function combinator)))
  p/HideOperator
  (-hide [flux] (.hide flux))
  p/TakeUntilOperator
  (-take-until [flux other] (.takeUntilOther flux other)))

(extend-type Mono
  p/MapOperator
  (-map [mono transformer]
    (.map mono (f/as-function transformer)))
  p/FilterOperator
  (-filter [mono predicate] (.filter mono (f/as-predicate predicate)))
  p/ZipOperator
  (-zip-with [mono other combinator] (.zipWith mono other (f/as-bi-function combinator)))
  p/HideOperator
  (-hide [mono] (.hide mono))
  p/TakeUntilOperator
  (-take-until [mono other] (.takeUntilOther mono other)))

(defn- inst->delay
  "Converts an inst to delay"
  [x]
  (-> (Instant/ofEpochMilli x)
      (.minusMillis (System/currentTimeMillis))))

(defn- duration ^Duration
  [d]
  (if (instance? Duration d) d (Duration/ofMillis d)))

(defn- delay-duration ^Duration
  [delay]
  (cond
    (inst? delay)
    (Duration/ofMillis (inst->delay delay))
    :else
    (duration delay)))

(defn interval
  ([period]
    (Flux/interval (duration period)))
  ([delay period]
   (Flux/interval (delay-duration delay) (duration period))))

(defn delay
  [delay]
  (Mono/delay (delay-duration delay)))

(defn concat
  [sources]
  (Flux/concat ^Iterable sources))

(defn mono-processor
  ([]
   (mono-processor nil))
  ([source]
   (cond (instance? MonoProcessor source) source
         (instance? Mono source) (.toProcessor ^Mono source)
         (instance? Flux source) (.publishNext ^Flux source)
         (nil? source) (MonoProcessor/create))))

(defn create
  ([emitter]
   (create emitter :buffer))
  ([emitter back-pressure]
   (create emitter back-pressure false))
  ([emitter back-pressure push?]
   (let [consumer (f/as-consumer emitter)]
     (if push?
       (Flux/push consumer)
       (Flux/create consumer)))))

(defn zip
  ([sources zipper]
   (cond
     (= 2 (count sources))
     (p/-zip-with (first sources) (second sources) zipper)
     ;(Flux/zip ^Publisher (first sources) ^Publisher (second sources) (f/as-bi-function zipper))
     :else
     (Flux/zip ^Iterable sources (f/as-function zipper))))
  ([sources zipper prefetch]
   (cond
     (array? sources)
     (Flux/zip (f/as-function zipper) ^int prefetch ^"[Lorg.reactivestreams.Publisher;" (to-array sources))
     :else
     (Flux/zip ^Iterable sources ^int prefetch (f/as-function zipper)))))

(defn range
  [start count]
  (Flux/range start count))

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

;; conditional
(defn take-while
  [p ^Flux flux]
  (.takeWhile flux (f/as-predicate p)))

;; transforming
(defn map
  [mapper publisher]
  (p/-map publisher mapper))

(defn mapcat
  ([mapper publisher]
   (p/-concat-map publisher mapper))
  ([mapper prefetch publisher]
   (p/-concat-map publisher mapper prefetch)))

(defn flat-map
  [mapper publisher]
  (p/-flat-map publisher mapper))

(defn flat-map-sequential
  [mapper ^Flux flux]
  (.flatMapSequential flux (f/as-function mapper)))

;; utility
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
   (.reduce flux (f/as-bi-function aggregator)))
  ([accumulator initial ^Flux flux]
   (.reduce flux initial (f/as-bi-function accumulator))))

(defn subscribe-with
  [^Subscriber s ^Publisher p]
  (.subscribe p s)
  s)

(defn subscribe
  ([^Publisher p]
   (subscribe nil nil nil p))
  ([on-next ^Publisher p]
   (subscribe on-next nil p))
  ([on-next on-error ^Publisher p]
   (subscribe on-next on-error nil p))
  ([on-next on-error on-complete ^Publisher p]
   (subscribe on-next on-error on-complete nil p))
  ([on-next on-error on-complete on-subscribe ^Publisher p]
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
     (subscribe-with (reify Subscriber
                       (onNext [_ n] (on-next n))
                       (onError [_ e] (on-error e))
                       (onComplete [_] (on-complete))
                       (onSubscribe [_ s] (on-subscribe s))) p))))



