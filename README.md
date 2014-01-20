Untyped
==========
Relational lambda calculus interpreter.
Successor and summator
----------------------
```clojure
(use 'untyped.core)

(first (run* [q]
  (eval-expo
    '((fn [n] (fn [f] (fn [x] (f ((n f) x))))) ;; Church numerals successor
      (fn [f] (fn [x] (f (f (f x))))))         ;; Number three (3)
    q)))                                       ;; What is the result?

=> (fn [f] (fn [x] (f (f (f (f x))))))       ;; Four f's (we got 4)


(first (run 1 [q] (eval-expo `((~ch-plus ~ch-three) ~ch-two) '() q)))

=> (fn [f] (fn [x] (f (f (f (f (f x)))))))   ;; Yes, five
```
Running backwards
-----------------
With this version I was able to generate number three with expression (ch-succ q)=ch-four.
However it took 77 seconds (with more eager evaluation I couldn't wait for it to stop).
```clojure
(time (first (run 1 [q] (eval-expo `(~ch-succ ~q) '() ch-four))))
"Elapsed time: 77346.962792 msecs"

((fn [_0] (fn [_1] (_0 (_0 (_0 _1))))) :- (!= (_1 f)) #<core$symbol_QMARK_ clojure.core$symbol_QMARK_@7116778b> (!= (_1 n)) (!= (_0 _1)) #<core$not_fn_QMARK_ untyped.core$not_fn_QMARK_@62a49a92>)
```

Currently, I can't wait it to generate successor function.

Future work
-----------
- No explicit parentheses
- No Clojure syntax (lambdas)
- Run backwards (generate combinators, for example 'eater' Fx=F)
