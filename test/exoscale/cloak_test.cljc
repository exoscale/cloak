(ns exoscale.cloak-test
  (:require [clojure.pprint :as pp]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [clojure.test.check.generators]
            [exoscale.cloak :as secret]))

(deftest secret-test
  (let [x "foo"
        s (secret/mask x)]
    #?(:bb (is (= "foo" (secret/reveal s)))
       :clj (is (= "foo" @s)))
    (is (nil? (str/index-of (pr-str s) "foo")))
    (is (nil? (str/index-of (str s) "foo")))
    (is (= "\"<< cloaked >>\"\n" (with-out-str (pp/pprint s))))

    (is (= nil (secret/unmask nil)))
    (is (= 1 (secret/unmask 1)))
    (is (= "foo" (secret/unmask s)))
    (is (= {:a "foo"} (secret/unmask {:a s})))
    (is (= {:a {:b {:c [1 "foo"]}}} (secret/unmask {:a {:b {:c [1 s]}}})))
    (defrecord F [s])
    #?(:bb (is (= (->F (secret/reveal s)) (secret/unmask (->F s))))
       :clj (is (= (->F @s) (secret/unmask (->F s)))))
    (is (= {:a 1} (secret/unmask (secret/mask {:a (secret/mask 1)}))))))

#?(:bb ()
   :clj
   (deftest compare-test
     (let [x (secret/mask "x")
           y (secret/mask "y")]
       (is (= [x y] (sort [x y])))
       (is (zero? (compare x y)))
       (is (zero? (compare y x))))))

(deftest double-masking-test
  (testing "masking a secret twice requires only a single unmasking"
    (is (= "foo" (secret/unmask (secret/mask (secret/mask "foo")))))))

(deftest predicate-test
  (is (secret/secret? (secret/mask "foo")))
  (is (false? (secret/secret? "not-a-secret"))))

(deftest gen-test
  (is (every? secret/secret? (map first (s/exercise ::secret/secret)))))
