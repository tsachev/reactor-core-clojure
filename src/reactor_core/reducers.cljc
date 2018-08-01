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
  ^{:doc    "reduce(rs) support."
    :author "Vladimir Tsanev"}
  reactor-core.reducers
  (:refer-clojure :exclude [reduce])
  (:require [reactor-core.publisher :refer [reduce]]
            #?(:cljs ["reactor-core-js/flux" :refer [Flux]]))
  #?(:clj
     (:import (reactor.core.publisher Flux))))

#?(:cljs
   (extend-protocol IReduce
     Flux
     (-reduce
       ([this f]
        (reduce this f))
       ([this f start]
        (reduce this start f))))
   :clj
   (extend-protocol clojure.core.protocols/CollReduce
     Flux
     (coll-reduce
       ([this f]
        (reduce this f))
       ([this f start]
        (reduce this start f)))))
