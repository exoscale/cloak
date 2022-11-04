(ns exoscale.cloak
  (:require [clojure.pprint :as pp]
            [clojure.walk :as walk]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.alpha :as s]))

(deftype Secret [x]
  Object (toString [_] "<< cloaked >>")
  clojure.lang.IDeref
  (deref [this] x)
  clojure.lang.IPending
  (isRealized [this] false)
  Comparable
  (compareTo [this other] 0)) ; to make with with seql h2/mysql compat

(defmethod pp/simple-dispatch Secret
  [x]
  (pr (str x)))

#?(:clj
   (do
     (defmethod print-method Secret [o ^java.io.Writer w]
       (.write w "\"")
       (.write w (str o))
       (.write w "\""))

     (prefer-method print-method Secret Object))
   :cljs
   (extend-protocol IPrintWithWriter
       Secret
       (-pr-writer [new-obj writer _]
         (write-all writer "#myObj \"" (:details new-obj) "\""))))

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
  (walk/postwalk #(if (instance? Secret %)
                    (unmask (deref %))
                    %)
                 x))

(defn secret? [x]
  (instance? Secret x))

(s/def ::secret
  (s/with-gen secret?
    #(gen/fmap mask (s/gen any?))))
