(ns exoscale.cloak
  (:require [clojure.pprint :as pp]
            [clojure.walk :as walk]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test.check.generators]
            [clojure.spec.alpha :as s])
  #?(:cljs (:refer-clojure :exclude [mask])))

(deftype Secret [x]
  Object
  (toString [_] "<< cloaked >>")
  #?@(:clj
      (clojure.lang.IDeref
       (deref [this] x)
       clojure.lang.IPending
       (isRealized [this] false)
       Comparable
       (compareTo [this other] 0)) ;; to make compatible with seql h2/mysql

      :cljs
      (IDeref
       (-deref [this] x)
       IPending
       (-realized? [this] false)
       IComparable
       (-compare [this other] 0))))

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
         (write-all writer "\"" (str new-obj) "\""))))

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
