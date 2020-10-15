(ns exoscale.cloak.test.core-test
  (:require [clojure.test :refer :all]
            [exoscale.cloak :as secret]
            [clojure.string :as str]
            [clojure.pprint :as pp]))

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
