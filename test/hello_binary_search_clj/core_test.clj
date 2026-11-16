(ns hello-binary-search-clj.core-test
  (:require [clojure.test :refer [deftest is testing run-tests]]
            [hello-binary-search-clj.core :refer [binary-search-leftmost lower-bound]]))

;; --- example-based cases, mirroring the Python/JS ports -------------------

(deftest empty-collection
  (is (= -1 (binary-search-leftmost [] 1))))

(deftest single-element
  (is (= 0 (binary-search-leftmost [7] 7)))
  (is (= -1 (binary-search-leftmost [7] 8))))

(deftest exact-match-unique
  (is (= 2 (binary-search-leftmost [1 3 5 7 9] 5)))
  (is (= 0 (binary-search-leftmost [1 3 5 7 9] 1)))
  (is (= 4 (binary-search-leftmost [1 3 5 7 9] 9))))

(deftest not-found
  (is (= -1 (binary-search-leftmost [2 4 6] 1)))  ; smaller than all
  (is (= -1 (binary-search-leftmost [2 4 6] 7)))  ; larger than all
  (is (= -1 (binary-search-leftmost [2 4 6] 5)))) ; between elements

(deftest leftmost-of-duplicates
  (is (= 1 (binary-search-leftmost [1 2 2 2 3] 2)))
  (is (= 0 (binary-search-leftmost [2 2 2 2] 2)))
  (is (= 2 (binary-search-leftmost [1 2 3 3 4] 3))))

(deftest duplicates-at-start-and-end
  (is (= 0 (binary-search-leftmost [3 3 4 5] 3)))
  (is (= 2 (binary-search-leftmost [1 2 3 3] 3))))

(deftest all-same-elements
  (is (= 0 (binary-search-leftmost [5 5 5 5 5] 5))))

(deftest even-length-collection
  (is (= 1 (binary-search-leftmost [1 2 2 3 4 5] 2)))
  (is (= 4 (binary-search-leftmost [1 2 2 3 4 5] 4))))

(deftest strings
  (is (= 1 (binary-search-leftmost ["a" "b" "b" "c"] "b"))))

(deftest keywords
  (is (= 1 (binary-search-leftmost [:a :b :b :c] :b))))

(deftest floating-point-numbers
  (is (= 1 (binary-search-leftmost [0.5 1.5 1.5 2.5] 1.5))))

(deftest negative-numbers
  (is (= 1 (binary-search-leftmost [-5 -3 -3 -1 0] -3))))

(deftest zero-and-boundary-values
  (is (= 0 (binary-search-leftmost [0 0 1] 0)))
  (is (= 1 (binary-search-leftmost [-1 0 1] 0))))

(deftest large-collection-with-duplicates
  (let [values (vec (for [i (range 100000)] (quot i 100)))] ; 0..999, 100 copies each
    (is (= 0 (binary-search-leftmost values 0)))
    (is (= 50000 (binary-search-leftmost values 500)))
    (is (= 99900 (binary-search-leftmost values 999)))
    (is (= -1 (binary-search-leftmost values 1000)))))

(deftest large-collection-even-numbers-only
  (let [values (vec (range 0 20000 2))]
    (is (= 0 (binary-search-leftmost values 0)))
    (is (= 617 (binary-search-leftmost values 1234)))
    (is (= -1 (binary-search-leftmost values 1235))) ; odd, absent
    (is (= 9999 (binary-search-leftmost values 19998)))
    (is (= -1 (binary-search-leftmost values 20000)))))

(deftest duplicates-everywhere
  (let [values (vec (concat (repeat 50 1) (repeat 50 2) (repeat 50 3)))]
    (is (= 0 (binary-search-leftmost values 1)))
    (is (= 50 (binary-search-leftmost values 2)))
    (is (= 100 (binary-search-leftmost values 3)))
    (is (= -1 (binary-search-leftmost values 4)))
    (is (= -1 (binary-search-leftmost values 0)))))

(deftest every-element-found-at-its-first-occurrence
  (let [values [1 1 2 3 3 3 4 5 5 6]]
    (doseq [[i v] (map-indexed vector values)]
      (when (or (zero? i) (not= (values (dec i)) v))
        (is (= i (binary-search-leftmost values v)))))))

;; --- lower-bound -----------------------------------------------------------

(deftest lower-bound-basics
  (is (= 0 (lower-bound [] 1)))
  (is (= 0 (lower-bound [5 6 7] 1)))
  (is (= 0 (lower-bound [5 6 7] 5)))
  (is (= 2 (lower-bound [1 3 5] 4)))
  (is (= 1 (lower-bound [1 3 5] 3)))
  (is (= 3 (lower-bound [1 2 3] 4)))
  (is (= 2 (lower-bound [1 2 3] 3))))

(deftest lower-bound-duplicates-and-all-equal-runs
  (is (= 1 (lower-bound [1 2 2 2 3] 2)))
  (is (= 0 (lower-bound [2 2 2] 2)))
  (is (= 3 (lower-bound [1 1 1] 2)))
  (is (= 0 (lower-bound [4 4 4 4] 4)))
  (is (= 0 (lower-bound [4 4 4 4] 3)))
  (is (= 4 (lower-bound [4 4 4 4] 5))))

(deftest lower-bound-negative-numbers
  (is (= 1 (lower-bound [-5 -3 -1] -4)))
  (is (= 0 (lower-bound [-5 -3 -1] -5))))

;; --- property-style checks with a seeded RNG (deterministic) ----------------

(defn- next-int [^java.util.Random rng n]
  (.nextInt rng n))

(deftest random-property-matches-first-occurrence
  (let [rng (java.util.Random. 42)]
    (dotimes [_ 1000]
      (let [n      (next-int rng 60)
            values (vec (sort (repeatedly n #(next-int rng 10))))
            target (next-int rng 10)
            ;; PersistentVector implements java.util.List -> indexOf is the
            ;; first-occurrence oracle.
            expected (.indexOf values target)]
        (is (= expected (binary-search-leftmost values target)))))))

(deftest random-property-lower-bound-is-count-of-elements-strictly-less
  (let [rng (java.util.Random. 1234)]
    (dotimes [_ 1000]
      (let [n      (next-int rng 80)
            values (vec (sort (repeatedly n #(next-int rng 20))))
            target (next-int rng 20)
            expected (count (filter #(< % target) values))]
        (is (= expected (lower-bound values target)))))))

;; --- exhaustive check of every sorted multiset up to length 7 ---------------

(defn- combinations-with-replacement [alphabet n]
  (lazy-seq
    (if (zero? n)
      '(())
      (for [x alphabet
            combo (combinations-with-replacement (filter #(>= % x) alphabet) (dec n))]
        (cons x combo)))))

(deftest exhaustive-all-sorted-multisets-up-to-length-7
  (doseq [length (range 0 8)
          combo (combinations-with-replacement [0 1 2 3] length)
          :let [values (vec combo)]
          target [0 1 2 3 4]] ; 4 is always absent
    (is (= (.indexOf values target) (binary-search-leftmost values target))
        (str "values=" values " target=" target))))

;; --- test runner (non-zero exit on failure, for CI) -------------------------

(defn -main [& _]
  (let [{:keys [fail error]} (run-tests 'hello-binary-search-clj.core-test)]
    (System/exit (if (zero? (+ (or fail 0) (or error 0))) 0 1))))
