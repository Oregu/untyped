Untyped
==========
Relational lambda calculus interpreter.
Successor and summator
----------------------
```clojure
(use 'untyped.core)

(first (run* [q] (nom/fresh [n f x]
  (eval-expo
    (app
      (lam n (lam f (lam x (app f (app (app n f) x))))) ;; Church numerals successor
        (lam f (lam x (app f (app f (app f x))))))      ;; Number three (3)
    q))))                                               ;; What is the result?

=> (fn [a_0] (fn [a_1] (a_0 (a_0 (a_0 (a_0 a_1))))))    ;; Four f's (we got 4)

(first (run* [q] (nom/fresh [m n f x]
  (eval-expo
    (app (app (ch+ m n f x) (ch 3 f x)) (ch 2 f x))     ;; What is 3 plus 2?
    q))))

=> (fn [a_0] (fn [a_1] (a_0 (a_0 (a_0 (a_0 (a_0 a_1)))))))  ;; Yes, 5
```
Nominal logic programming made it a bit clumsy, how can it can be improved to look cleaner?

Running backwards
-----------------
We can decrement with successor: (ch-succ q)=ch4 => q=ch3.  
And subtract with summator: (ch+ q ch2)=ch6 => q=ch4.  
For number four it used one second, but generating Church 5 takes 40 sec.
```clojure
(time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
  (app (ch-succ n f x) q) ; succ ? =
  (ch4 f1 x1))))))        ; 4
"Elapsed time: 891.251582 msecs"

=> (fn [a_0] (fn [a_1] (a_0 (a_0 (a_0 a_1))))) ; Three a_0

(time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
  (app (ch-succ n f x) q) ; succ ? =
  (ch6 f1 x1))))))        ; 6
"Elapsed time: 37273.876513 msecs"

=> (fn [a_0] (fn [a_1] (a_0 (a_0 (a_0 (a_0 (a_0 a_1))))))) ; Five a_0
```

How about producing successor function?
```clojure
(time (first (run 1 [q]
  (nom/fresh [f x f1 x1]
    (eval-expo (app q (ch 0 f x) (ch 1 f1 x1)))   ; ? 0 = 1
    (eval-expo (app q (ch 1 f x)) (ch 2 f x)))))) ; ? 1 = 2
"Elapsed time: 41572.578565 msecs"

(fn [a_0] (fn [a_1] (fn [a_2] ((a_0 a_1) (a_1 a_2)))))
; ie: (fn [n] (fn [f] (fn [x] ((n f) (f x)))))
```
41 seconds? In versions without noms I didn't ever got an answer!  
A way to go for sure.  
Actully canonical succ function is
```clojure
(fn [n] (fn [f] (fn [x] (n f x))))
```
but the one I have seems to work too.  
Probably canonical one is optimized for Church numerals, but I'm not sure. Investigating on it.

Combinatory logic
-----------------
We can easily generate λ-quine, which Ω:
```clojure
(run 2 [q] (eval-expo q q))
=> ((fn [a_0] _1)
     (((fn [a_0] a_0) (fn [a_1] (a_1 a_1)))
      ((fn [a_0] a_0) (fn [a_1] (a_1 a_1)))))
```
We have two answers back, first one is an abstraction, which is a value and therefore evaluates to itself. And second one is (slightly un-normalised) big omega.

Branches
--------
- [master](https://github.com/Oregu/untyped) — current approach is nominal logic programming.
- [naive](https://github.com/Oregu/untyped/tree/naive) — evaluating expression with explicit substitution step. Not capture avoiding. Produces Church numeral 3 in half a second.
- [eval-only](https://github.com/Oregu/untyped/tree/eval-only) — eval-expo with passing environment around. Messy. Not capture avoiding. Produces Church numeral 3 in 2 seconds.
- [cas](https://github.com/Oregu/untyped/tree/cas) — attempt to add capture avoiding step. Perfomance problems. Result looks weird. In progress.

Future work
-----------
- No explicit parentheses
- Lambdas instead of ‘fn’ syntax
- Run backwards (generate combinators, for example ‘greedy’/‘eater’/‘K-∞’ Fx=F, if one exist)

Resources
---------
[alphaKanren](https://github.com/webyrd/alphaKanren) (with examples producing Ω and Y)  
[Nominal Wiki](https://github.com/clojure/core.logic/wiki/core.logic.nominal)  
[Nominal @namin talk](https://github.com/namin/minikanren-confo/blob/master/src/talk.clj)  
[Introduction to Lambda Calculus](http://www.cse.chalmers.se/research/group/logic/TypesSS05/Extra/geuvers.pdf) — the best I've read.  
[Benjamin C. Pierce TAPL](http://www.cis.upenn.edu/~bcpierce/tapl/) — the best I've read.  

Notes
-----
“This final version of appendo illustrates an important principle: unifications should always come before recursive calls, or calls to other “serious” relations.”  
(From WIll Byrd's “Relational Programming in miniKanren: Techniques, Applications, and Implementations.” (pdf)[http://gradworks.umi.com/3380156.pdf]).
