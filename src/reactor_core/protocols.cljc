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
  ^{:doc    "Operator protocols"
    :author "Vladimir Tsanev"}
  reactor-core.protocols
  (:require [reactive-streams.core :refer [IPublisher]]))

;; Transforming Operators

(defprotocol MapOperator
  "Transform items emitted by a Publisher by applying function to each item"
  (-map [publisher transformer]))

(defprotocol FlatMapOperator
  "Transform items emitted by a Publisher into Publishers, then flatten the emissions
   from those into single Publisher"
  (-flat-map [publisher transformer])
  (-concat-map [publisher transformer] [publisher transformer prefetch]))

;; Filtering Operators

(defprotocol FilterOperator
  "Emit only those items from a Publisher that pass a predicate test"
  (-filter [publisher predicate]))

(defprotocol FirstOperator
  "Emit only the first item emitted by a Publisher"
  (-first [publisher] [publisher default-value]))

(defprotocol IgnoreElementsOperator
  "Do not emit any items from a Publisher but mirror its termination signal"
  (-ignore-elements [publisher]))

(defprotocol LastOperator
  "Emit only the last item emitted by a Publisher"
  (-last [publisher] [publisher default-value]))

(defprotocol SkipOperator
  "Suppress the first n items emitted by a Publisher"
  (-skip [publisher n]))

(defprotocol SkipLastOperator
  "Suppress the final n items emitted by a Publisher"
  (-skip-last [publisher n]))

(defprotocol TakeOperator
  "Emit only the first n items emitted by a Publisher"
  (-take [publisher n]))

(defprotocol TakeLastOperator
  "Emit only the last n items emitted by a Publisher"
  (-take-last [publisher n]))

;; Combining Operators

(defprotocol ZipOperator
  "Combine the emissions of multiple Publishers together via a specified function
   and emit single items for each combination based on the results of this function"
  (-zip-with
    [publisher other combinator]
    [publisher other prefetch combinator]))

(defn- zip-dispatch
  "should support
  (-zip p1 p2 inc)
  (-zip [p1 p2 p3 p4] vector)
  (-zip [p1 p2 p3 p4] vector :delay-error)"
  [f & args]
  (if (seqable? f)
    [(type (first f)) (count f)]
    [(type f)
     (->> args
          (take-while #(satisfies? IPublisher %))
          (count)
          (inc))]))

(defmulti -zip zip-dispatch)

;; Error Handling Operators

;; Utility

(defprotocol HideOperator
  (-hide [publisher]))

;; Conditional and Boolean Operators

(defprotocol TakeUntilOperator
  "Discard any items emitted by a Publisher after a second Publisher emits an item or terminates"
  (-take-until [publisher other]))

(defprotocol TakeWhileOperator
  "Mirror items emitted by a Publisher until a specified condition becomes false"
  (-take-while [publisher predicate]))
