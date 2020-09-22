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
    (is (= "\"<<-secret->>\"\n" (with-out-str (pp/pprint s))))

    (is (= nil (secret/unmask nil)))
    (is (= 1 (secret/unmask 1)))
    (is (= "foo" (secret/unmask s)))
    (is (= {:a "foo"} (secret/unmask {:a s})))
    (is (= {:a {:b {:c [1 "foo"]}}} (secret/unmask {:a {:b {:c [1 s]}}})))
    (defrecord F [s])
    (is (= (->F @s) (secret/unmask (->F s))))))

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





