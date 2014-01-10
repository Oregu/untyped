Untyped
==========

Relational lambda calculus interpreter.

What is already working:
```clojure
(run* [q]
  (eval-expo
    '((fn [n] (fn [f] (fn [x] (f ((n f) x))))) ;; Church numbers successor
      (fn [f] (fn [x] (f (f (f x))))))         ;; Number three (3)
    q))                                        ;; What is the result?


=> ((fn [f] (fn [x] (f (f (f (f x)))))))       ;; Four f's (we got 4)
```
Ok, straight evaluation is working (for Church numbers and successor at least).
