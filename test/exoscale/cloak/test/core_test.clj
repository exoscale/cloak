(ns exoscale.cloak.test.core-test
  (:require [clojure.test :refer :all]
            [exoscale.cloak :as secret]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [clojure.spec.alpha :as s]))

(deftest secret-test
  (let [x "foo"
        s (secret/mask x)]
    (is (= "foo" @s))
    (is (nil? (str/index-of (pr-str s) "foo")))
    (is (nil? (str/index-of (str s) "foo")))
    (is (= "\"<< cloaked >>\"\n" (with-out-str (pp/pprint s))))

    (is (= nil (secret/unmask nil)))
    (is (= 1 (secret/unmask 1)))
    (is (= "foo" (secret/unmask s)))
    (is (= {:a "foo"} (secret/unmask {:a s})))
    (is (= {:a {:b {:c [1 "foo"]}}} (secret/unmask {:a {:b {:c [1 s]}}})))
    (defrecord F [s])
    (is (= (->F @s) (secret/unmask (->F s))))))

(deftest compare-test
  (let [x (secret/mask "x")
        y (secret/mask "y")]
    (is (= [x y] (sort [x y])))
    (is (zero? (.compareTo ^Comparable x
                           ^Comparable y)))
    (is (zero? (.compareTo ^Comparable y
                           ^Comparable x)))))

(deftest double-masking-test
  (testing "masking a secret twice requires only a single unmasking"
    (is (= "foo" (secret/unmask (secret/mask (secret/mask "foo")))))))

(deftest predicate-test
  (is (secret/secret? (secret/mask "foo")))
  (is (false? (secret/secret? "not-a-secret"))))

(deftest gen-test
  (is (every? secret/secret? (map first (s/exercise ::secret/secret)))))

(deftest secret-equality
  (testing "equality must succeed for same secret value"
    (let [x (rand-int 1000)
          s1 (secret/mask x)
          s2 (secret/mask x)]
      (is (= s1 s2))
      (is (= (.hashCode s1) (.hashCode s2)))))
  (testing "equality must fail for different secret value"
    (let [s1 (secret/mask "foo")
          s2 (secret/mask "bar")]
      (is (not= s1 s2))
      (is (not= (.hashCode s1) (.hashCode s2))))))