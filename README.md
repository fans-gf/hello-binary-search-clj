# hello-binary-search-clj

[![tests](https://github.com/fans-gf/hello-binary-search-clj/actions/workflows/test.yml/badge.svg)](https://github.com/fans-gf/hello-binary-search-clj/actions/workflows/test.yml)

Leftmost-exact-match binary search in Clojure.

Given a collection sorted in non-decreasing order (according to `compare`),
`binary-search-leftmost` returns the index of the **first** occurrence of
`target`, or `-1` if absent — in O(log n) time and O(1) space.

## Usage

```clojure
(require '[hello-binary-search-clj.core :refer [binary-search-leftmost lower-bound]])

(binary-search-leftmost [1 2 2 2 3] 2) ; => 1 (leftmost exact match)
(binary-search-leftmost [1 2 3] 4)     ; => -1 (absent)
(binary-search-leftmost ["a" "b" "b"] "b") ; => 1
(binary-search-leftmost [:a :b :b :c] :b)  ; => 1

;; lower-bound: insertion point (first index with element >= target),
;; in [0, (count values)] — like bisect_left.
(lower-bound [1 2 2 3] 2) ; => 1
(lower-bound [1 2 3] 4)   ; => 3
```

**Preconditions**

- `values` must be sorted in non-decreasing order according to `compare`;
  the precondition is not validated and behavior on unsorted input is
  undefined (no exception is raised).
- Anything `compare` works on is supported: numbers, strings, keywords,
  symbols, vectors, etc.
- Pass a vector (or another indexed collection) for O(log n) overall. `nth`
  on a seq is O(n), so passing a long seq degrades to linear scans per probe.

## Running the tests

Requires a JDK and the [Clojure CLI](https://clojure.org/guides/install_clojure).
No other dependencies — tests use the built-in `clojure.test`.

```bash
clojure -M:test
```

Tests cover empty/single/even-length collections, duplicates at
start/middle/end, boundary values, large vectors (100k elements), an
exhaustive check of every sorted multiset of length ≤ 7 over `{0,1,2,3}`
(3708 assertions in total), seeded randomized property tests against the
`java.util.List/indexOf` first-occurrence oracle (all Clojure vectors
implement `java.util.List`), and a `lower-bound` check against the
count-of-elements-strictly-less-than-target oracle.

Tests also run on every push and pull request via GitHub Actions (see
[.github/workflows/test.yml](.github/workflows/test.yml)).

## Benchmarks

`clojure -M bench/benchmark.clj` (OpenJDK 25 / Temurin, best-of-5 after
warmup; 1,024 seeded targets cycled, 51,200 ops per rep). Example
measurement on the author's machine — numbers are machine-specific, re-run
to reproduce. JVM warmup strongly affects short runs; for rigorous JVM
benchmarking use JMH.

| Scenario | n | ns/op |
|---|---|---|
| hit, unique elements | 1,000,000 | ~573 |
| miss, odd target in even array | 1,000,000 | ~591 |
| hit, leftmost of duplicates | 1,000,000 | ~687 |
| hit, small array | 100 | ~102 |
