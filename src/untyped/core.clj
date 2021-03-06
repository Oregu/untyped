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

(defn eval-exp-stepo [exp val]
  (conde
    [(fresh [rator rand r1 body]
      (nom/fresh [x]
        (appo rator rand exp)
        (lamo x body r1)
        (eval-exp-stepo rator r1)
        (substo body rand x val)))]
    [(fresh [rator rand r1]
      (appo rator rand exp)
      (appo r1 rand val)
      (eval-exp-stepo rator r1))]
    [(fresh [rator rand r2]
      (appo rator rand exp)
      (appo rator r2 val)
      (eval-exp-stepo rand r2))]))

(defn step==o [t1 t2]
  (conde
    [(== t1 t2)]
    [(fresh [t1+]
      (eval-exp-stepo t1 t1+)
      (step==o t1+ t2))]
    [(fresh [t2+]
      (eval-exp-stepo t2 t2+)
      (step==o t1 t2+))]))

; Will's Reducer. Can produce recursive functions.
; Borrowed here: https://github.com/webyrd/alphaKanren/blob/master/tests.scm

(defn substo-W [id-tm E out]
  (conde
    [(nom/fresh [a]
      (== (tie a a) id-tm)
      (== E out))]
    [(nom/fresh [a]
      (fresh [B]
        (nom/hash a B)
        (== (tie a B) id-tm)
        (== B out)))]
    [(nom/fresh [a b]
      (fresh [E1 E1+]
        (nom/hash b E)
        (== (tie a (lam b E1)) id-tm)
        (lamo b E1+ out)
        (substo-W (tie a E1) E E1+)))]
    [(nom/fresh [a]
      (fresh [E1 E2 E1+ E2+]
        (== (tie a (app E1 E2)) id-tm)
        (appo E1+ E2+ out)
        (substo-W (tie a E1) E E1+)
        (substo-W (tie a E2) E E2+)))]))

(defn betao [t1 E2+]
  (nom/fresh [b]
    (fresh [E E+]
      (appo (lam b E) E+ t1)
      (substo-W (tie b E) E+ E2+))))

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
