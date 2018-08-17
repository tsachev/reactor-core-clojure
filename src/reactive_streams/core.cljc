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
  reactive-streams.core)

(defprotocol ISubscription
  ""
  (request [subscription n])
  (cancel [subscription]))

(defprotocol ISubscriber
  (on-subscribe [subscriber ^ISubscription subscription])
  (on-next [subscriber item])
  (on-error [subscriber error])
  (on-complete [subscriber]))

(defprotocol IPublisher
  (subscribe [publisher ^ISubscriber subscriber]))

(defn processor? [p]
  (and (satisfies? IPublisher p)
       (satisfies? ISubscriber p)))
