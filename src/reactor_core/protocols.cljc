(ns
  ^{:doc    "Flux/Mono conversion protocols"
    :author "Vladimir Tsanev"}
  reactor-core.protocols
  #?(:cljs (:require ["reactor-core-js/flux" :refer [Flux Mono]])
     :clj  (:import (reactor.core.publisher Flux Mono))))

(defprotocol Fluxable
  (to-flux ^Flux [_] "Provides a conversion to Flux."))

(defprotocol Monoable
  (to-mono ^Mono [_] "Provides a conversion to Mono."))
