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
    '() q)))                                   ;; What is the result?

=> (fn [f] (fn [x] (f (f (f (f x))))))       ;; Four f's (we got 4)

(first (run* [q]
  (eval-expo
  `((~ch-plus ~ch-three) ~ch-two)
  '() q)))

=> (fn [f] (fn [x] (f (f (f (f (f x)))))))   ;; Yes, five
```
Running backwards
-----------------
It already can subtract using successor: (ch-succ q)=ch4.  
For number four it used just half a second, but generating Church 5 takes 20 sec.
```clojure
(time (first (run 1 [q] (eval-expo `(~ch-succ ~q) '() ch4))))
"Elapsed time: 578.228557 msecs"

((fn [_0] (fn [_1] (_0 (_0 (_0 _1)))))
:- (!= (_1 f)) #<core$symbol_QMARK_ clojure.core$symbol_QMARK_@7116778b> (!= (_1 n)) (!= (_0 _1)) #<core$not_fn_QMARK_ untyped.core$not_fn_QMARK_@62a49a92>)
```

It successfully produces Church successor function, but uses dark technics like not-avoiding capture substitution.  
Here is the successor func he thinks will suit me:
```clojure
(fn [n] ((fn [x] n) (f x)))
```
Wow…

Branches
--------
master — evaluating expression with explicit substitution step. Not capture avoiding. Produces Church numeral 3 in half a second.
eval-only — evaluating expression with eval-expo only, passing environment around. Messy. Not capture avoiding. Produces Church numeral 3 in 2 seconds.
cas — attempt to add capture avoiding step. Perfomance problems. Result looks weird. In progress.
nom — yet to be created. They say nominal logic can solve my problem.

Future work
-----------
- No explicit parentheses
- No Clojure syntax (lambdas)
- Run backwards (generate combinators, for example 'eater' Fx=F, if one exist)

Notes
-----
“This final version of appendo illustrates an important principle: unifications should always come before recursive calls, or calls to other “serious” relations.”  
(From WIll Byrd's “Relational Programming in miniKanren: Techniques, Applications, and Implementations.”)
