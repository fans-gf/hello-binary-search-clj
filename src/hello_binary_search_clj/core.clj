(ns hello-binary-search-clj.core
  "Binary search utilities.

  All functions assume the input is sorted in non-decreasing order according
  to `compare`. The precondition is NOT validated: behavior on unsorted
  input is undefined (no exception is raised; results are meaningless).

  Pass a vector (or another indexed collection with O(log n) or better
  lookup) for O(log n) overall; `nth` on a seq is O(n).")

(defn lower-bound
  "Return the index of the first element in sorted `values` that is >=
  `target` — equivalently, the insertion point of `target`. The returned
  index is in the range [0, (count values)]. O(log n) time, O(1) space.
  Behavior on unsorted input is undefined (no error is raised).

  (lower-bound [1 2 2 2 3] 2) => 1
  (lower-bound [1 2 3] 4)     => 3"
  [values target]
  (loop [left 0 right (count values)] ; invariant: answer is in [left, right]
    (if (< left right)
      (let [mid (quot (+ left right) 2)
            c   (compare (nth values mid) target)]
        (if (neg? c)
          (recur (inc mid) right)
          (recur left mid)))
      left)))

(defn binary-search-leftmost
  "Return the index of the first occurrence of `target` in sorted `values`,
  or -1 if `target` is not present. O(log n) time, O(1) space.
  Behavior on unsorted input is undefined (no error is raised).

  (binary-search-leftmost [1 2 2 2 3] 2) => 1
  (binary-search-leftmost [1 2 3] 4)     => -1"
  [values target]
  (let [index (lower-bound values target)]
    (if (and (< index (count values))
             (zero? (compare (nth values index) target)))
      index
      -1)))
