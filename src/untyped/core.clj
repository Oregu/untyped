(ns untyped.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]))

(defn not-fn? [x]
  (or (not (coll? x))
      (empty? x)
      (not= (first x) 'fn)))

(defn not-fno [x] (predc x not-fn? 'not-fn?))
(defn symbolo [x] (predc x symbol? 'symbol?))
(defn nomo    [x] (predc x nom?    'nom?))

(defn lam [x e] `(~'fn ~(tie x e)))
(defn app [e1 e2] `(~e1 ~e2))
(defn lamo [x e out] (== out (lam x e)))
(defn appo [e1 e2 out] (all (== out (app e1 e2)) (!= e1 'fn)))

(defn valo [e]
  (fresh [b]
    (nom/fresh [x]
      (lamo x b e))))

(defn substo [e new a out] ;; out == [new/a]e
  (conde
    [(nomo e) (== e a) (== new out)]
    [(nomo e) (!= e a) (== e out)]
    [(fresh [e1 e2 o1 o2 a1 a2 b]
      (appo e1 e2 e)
      (substo e1 new a o1)
      (substo e2 new a o2)
      (nom/fresh [x]
        (conde
          [(lamo x b o1)
           (substo b o2 x out)]
          [(not-fno o1)
           (appo o1 o2 out)])))]
    [(fresh [e0 o0]
      (nom/fresh [c]
        (lamo c e0 e)
        (lamo c o0 out)
        (nom/hash c a) ;; [new/c]λc.e ≡α λc.e
        (nom/hash c new) ;; [c/a]λc.a ≡α λa.c ≢α λc.c
        (substo e0 new a o0)))]))

(defn eval-expo [exp val]
  (conde
    [(valo exp) (== exp val)]
    [(fresh [rator rand r1 body]
      (nom/fresh [x]
        (appo rator rand exp)
        (lamo x body r1)
        (eval-expo rator r1)
        (substo body rand x val)))]))

; Will's Reducer. Can produce recursive functions.
; Borrowed it here: https://github.com/webyrd/alphaKanren/blob/master/tests.scm

(defn substo-W [e new a out] ;; out == [new/a]e
  (conde
    [(nomo e) (== e a) (== new out)]
    [(nomo e) (!= e a) (== e out)]
    [(fresh [e1 e2 o1 o2]
      (appo e1 e2 e)
      (appo o1 o2 out)
      (substo-W e1 new a o1)
      (substo-W e2 new a o2))]
    [(fresh [e0 o0]
      (nom/fresh [c]
        (lamo c e0 e)
        (lamo c o0 out)
        (nom/hash c a) ;; [new/c]λc.e ≡α λc.e
        (nom/hash c new) ;; [c/a]λc.a ≡α λa.c ≢α λc.c
        (substo-W e0 new a o0)))]))

(defn betao [t1 E2+]
  (nom/fresh [b]
    (fresh [E E+]
      (appo (lam b E) E+ t1)
      (substo-W E E+ b E2+))))

(defn stepo [t1 t2]
  (conde
    [(betao t1 t2)]
    [(fresh [M N M+]
      (appo M N t1)
      (appo M+ N t2)
      (stepo M M+))]
    [(fresh [M N N+]
      (appo M N t1)
      (appo M N+ t2)
      (stepo N N+))]))

(defn step-equalo [t1 t2]
  (conde
    [(== t1 t2)]
    [(fresh [t1+]
      (stepo t1 t1+)
      (step-equalo t1+ t2))]
    [(fresh [t2+]
      (stepo t2 t2+)
      (step-equalo t1 t2+))]))
