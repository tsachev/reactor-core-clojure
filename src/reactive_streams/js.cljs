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
  ^{:doc    "Reactive Streams Spec"
    :author "Vladimir Tsanev"}
  reactive-streams.js
  (:require [reactive-streams.core]))

(defprotocol Subscription
  (request [this ^number n])
  (cancel [this]))

(defprotocol Subscriber
  (onSubscribe [this ^Subscription s])
  (onNext [this t])
  (onError [this ^js/Error t])
  (onComplete [this]))

(defprotocol Publisher
  (subscribe [this ^Subscriber s]))

(defprotocol Processor)

(extend-protocol Publisher
  Processor)
(extend-protocol Subscriber
  Processor)

(defn ->ISubscriber [^Subscriber subscriber]
  (if (satisfies? reactive-streams.core/ISubscriber subscriber)
    subscriber
    (specify! subscriber
      reactive-streams.core/ISubscriber
      (on-subscribe [_ subscription] (onSubscribe subscriber subscription))
      (on-next [_ item] (onNext subscriber item))
      (on-error [_ error] (onError subscriber error))
      (on-complete [_] (onComplete subscriber)))))
