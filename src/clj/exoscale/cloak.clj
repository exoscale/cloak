(ns exoscale.cloak
  (:require [clojure.pprint :as pp]
            [clojure.walk :as walk]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.alpha :as s]))

(deftype Secret [x]
  Object
  (toString [_] "<< cloaked >>")
  (equals [this object]
    (and (instance? Secret object)
         (= x (.-x ^Secret object))))
  (hashCode [this]
    (.hashCode x))
  clojure.lang.IDeref
  (deref [this] x)
  clojure.lang.IPending
  (isRealized [this] false)
  Comparable
  (compareTo [this other] 0)) ; to make with with seql h2/mysql compat

(defmethod print-method Secret [o ^java.io.Writer w]
  (.write w "\"")
  (.write w (str o))
  (.write w "\""))

(prefer-method print-method Secret Object)
(.addMethod ^clojure.lang.MultiFn pp/simple-dispatch Secret #(pr (str %)))

(defn mask
  "Mask a value behind the `Secret` type, hiding its real value when printing"
  [x]
  (if (instance? Secret x)
    x
    (Secret. x)))

(defn unmask
  "Reveals all potential secrets from `x`, returning the value with secrets
  unmasked, works on any walkable type"
  [x]
  (walk/postwalk #(cond-> %
                    (instance? Secret %)
                    deref)
                 x))

(defn secret? [x]
  (instance? Secret x))

(s/def ::secret
  (s/with-gen secret?
    #(gen/fmap mask (s/gen any?))))
