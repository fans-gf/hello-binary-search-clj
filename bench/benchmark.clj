;; Benchmark binary-search-leftmost. Run: clojure -M bench/benchmark.clj
;;
;; Same methodology across the Python / JavaScript / Clojure repos:
;; fixed set of 1024 seeded-random targets per scenario (cycled),
;; 20,000 warmup ops, then 5 timed reps of 51,200 ops; report best-of-5 ns/op.
;; Comparison numbers are machine-specific — re-run locally to reproduce.
;;
;; NOTE: JVM warmup matters; for rigorous benchmarking use JMH.

(require '[hello-binary-search-clj.core :refer [binary-search-leftmost]])

(def ^:const reps 5)
(def ^:const batch (* 50 1024)) ;; 51,200 searches per timed rep

(defn bench [name data targets]
  (let [n (count targets)]
    ;; warmup — important for the JIT
    (dotimes [i 50000]
      (binary-search-leftmost data (nth targets (mod i n))))
    (let [best (reduce
                (fn [best _]
                  (let [start (System/nanoTime)]
                    (dotimes [i batch]
                      (binary-search-leftmost data (nth targets (mod i n))))
                    (min best (/ (- (System/nanoTime) start) (double batch)))))
                Double/POSITIVE_INFINITY
                (range reps))]
      (println (format "%-48s n=%-9d best=%9.1f ns/op  (%,.0f ops/s)"
                       name (count data) best (/ 1e9 best))))))

(let [rng (java.util.Random. 42)
      ri  (fn [n] (.nextInt rng n))]

  (let [full (vec (range 1000000))
        ts   (vec (repeatedly 1024 #(ri 1000000)))]
    (bench "hit, unique elements" full ts))

  (let [evens (vec (range 0 2000000 2))
        ts    (vec (repeatedly 1024 #(inc (* 2 (ri 1000000)))))]
    (bench "miss, odd target in even array" evens ts))

  (let [dups (vec (map #(quot % 50) (range 1000000)))
        ts   (vec (repeatedly 1024 #(ri 20000)))]
    (bench "hit, leftmost of duplicates" dups ts))

  (let [small (vec (range 100))
        ts    (vec (repeatedly 1024 #(ri 100)))]
    (bench "hit, small array" small ts)))
