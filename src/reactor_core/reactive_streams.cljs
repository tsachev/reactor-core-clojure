(ns
  ^{:doc    "Run cljs tests."
    :author "Vladimir Tsanev"}
  reactor-core.reactive-streams)

(defprotocol IPublisher
  (subscribe [publisher subscriber]))

(defprotocol ISubscriber
  (on-subscribe [subscriber subscription])
  (on-next [subscriber item])
  (on-error [subscriber subscription])
  (on-complete [subscriber]))

(defprotocol ISubscription
  ""
  (request [subscription n])
  (cancel [subscription]))
