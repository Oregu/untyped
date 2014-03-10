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
We can subtract using successor: (ch-succ q)=ch4.  
For number four it used one second, but generating Church 5 takes 40 sec.
```clojure
(time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
  (app (ch-succ n f x) q) ; succ ? =
  (ch4 f1 x1))))))        ; 4
"Elapsed time: 891.251582 msecs"

(fn [a_0] (fn [a_1] (a_0 (a_0 (a_0 a_1))))) ; Three a_0

(time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
  (app (ch-succ n f x) q) ; succ ? =
  (ch6 f1 x1))))))        ; 6
"Elapsed time: 37273.876513 msecs"

(fn [a_0] (fn [a_1] (a_0 (a_0 (a_0 (a_0 (a_0 a_1))))))) ; Five a_0
```

How about producing successor function?
```clojure
(time (first (run 1 [q]
  (nom/fresh [f x f1 x1]
    (eval-expo (app q (lam f (lam x x))) (lam f1 (lam x1 (app f1 x1)))))  ; ? 0 = 1
  (nom/fresh [f x f1 x1]
    (eval-expo (app q (lam f (lam x (app f x)))) (lam f1 (lam x1 (app f1 (app f1 x1)))))))))  ; ? 1 = 2
"Elapsed time: 41572.578565 msecs"

(fn [a_0] (fn [a_1] (fn [a_2] ((a_0 a_1) (a_1 a_2))))) ;; ie: (fn [n] (fn [f] (fn [x] ((n f) (f x)))))
```
41 seconds? In versions without noms I didn't ever got an answer!  
A way to go for sure.  
Actully canonical succ function is
```clojure
(fn [n] (fn [f] (fn [x] (n f x))))
```
but the one I have seems to work too.  
Probably canonical one is optimized for Church numerals, but I'm not sure. Investigating on it.

Branches
--------
- [master](https://github.com/Oregu/untyped) — current approach is nominal logic programming.
- [naive](https://github.com/Oregu/untyped/tree/naive) — evaluating expression with explicit substitution step. Not capture avoiding. Produces Church numeral 3 in half a second.
- [cas](https://github.com/Oregu/untyped/tree/cas) — attempt to add capture avoiding step. Perfomance problems. Result looks weird. In progress.
- [eval-only](https://github.com/Oregu/untyped/tree/eval-only) — eval-expo with passing environment around. Messy. Not capture avoiding. Produces Church numeral 3 in 2 seconds.

Future work
-----------
- No explicit parentheses
- Lambdas instead of ‘fn’ syntax
- Run backwards (generate combinators, for example ‘greedy’/‘eater’/‘K-∞’ Fx=F, if one exist)

Resources
---------
[Nominal Wiki](https://github.com/clojure/core.logic/wiki/core.logic.nominal)  
[Nominal @namin talk](https://github.com/namin/minikanren-confo/blob/master/src/talk.clj)  
[Introduction to Lambda Calculus](http://www.cse.chalmers.se/research/group/logic/TypesSS05/Extra/geuvers.pdf) — the best I've read.  
[Benjamin C. Pierce TAPL](http://www.cis.upenn.edu/~bcpierce/tapl/) — the best I've read.  

Notes
-----
“This final version of appendo illustrates an important principle: unifications should always come before recursive calls, or calls to other “serious” relations.”  
(From WIll Byrd's “Relational Programming in miniKanren: Techniques, Applications, and Implementations.”)
