(ns association.facts-test
  (:require [clojure.test :refer [deftest is]]
            [association.facts :as facts]))

(deftest afbf-has-spec-basis
  (let [sb (facts/spec-basis "afbf")]
    (is (= 2 (count sb)))
    (is (every? #(= "0150" (:association-rule/isic %)) sb))
    (is (every? #(= "USA" (:association-rule/country %)) sb))))

(deftest unknown-association-has-no-spec-basis
  (is (nil? (facts/spec-basis "wef")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["afbf" "wef"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["wef"] (:missing-associations c)))))

(deftest by-topic-filters
  (is (= ["afbf.what-we-do"]
         (mapv :association-rule/id (facts/by-topic "afbf" :advocacy))))
  (is (empty? (facts/by-topic "afbf" :labor)))
  (is (empty? (facts/by-topic "wef" :governance))))
