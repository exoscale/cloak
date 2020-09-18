(ns exoscale.cloak
  (:require [clojure.pprint :as pp]
            [clojure.walk :as walk]
            [clojure.spec.alpha :as s]))

(deftype Secret [x]
  Object (toString [_] "<<-secret->>")
  clojure.lang.IDeref
  (deref [this] x)
  clojure.lang.IPending
  (isRealized [this] false))

(defmethod print-method Secret [o ^java.io.Writer w]
  (.write w "\"")
  (.write w (str o))
  (.write w "\""))

(prefer-method print-method Secret Object)
(.addMethod ^clojure.lang.MultiFn pp/simple-dispatch Secret #(pr (str %)))

(defn mask
  "Mask a value behind the `Secret` type, hiding its real value when printing"
  [x]
  (Secret. x))

(defn unmask
  "Reveals all potential secrets from `x`, returning the value with secrets
  unmasked, works on any walkable type"
  [x]
  (walk/postwalk #(cond-> %
                    (instance? Secret %)
                    deref)
                 x))

(s/def ::secret #(instance? Secret %))
