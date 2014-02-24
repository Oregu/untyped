(ns untyped.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]))

(defn not-fn? [x]
  (or (not (coll? x))
      (empty? x)
      (not= (first x) 'fn)
      ; Doesn't work for tie
      #_(not (vector? (second x)))))

(defn not-fno [x] (predc x not-fn? 'not-fn?))
(defn symbolo [x] (predc x symbol? 'symbol?))

(defn reifier-for [tag x]
  (fn [c v r a]
    (let [x (walk* r (walk* a x))]
      (when (symbol? x)
        `(~tag ~x)))))

(defn lam [x e] `(~'fn ~(nom/tie x e)))
(defn app [e1 e2] `(~e1 ~e2))
(defn lamo [x e out] (== out (lam x e)))
(defn appo [e1 e2 out] (all (== out (app e1 e2)) (!= e1 'fn)))
(defn nomo [x] (predc x nom? (reifier-for 'nom x)))

(defn namin-substo [e new a out] ;; out == [new/a]e
  (conde
    [(nomo e) (== e a) (== new out)]
    [(nomo e) (!= e a) (== e out)]
    [(fresh [e1 e2 o1 o2]
       (appo e1 e2 e)
       (namin-substo e1 new a o1)
       (namin-substo e2 new a o2)
       (fresh [b]
         (nom/fresh [x]
           (conde
             [(lamo x b o1)
              (trace-lvars "equal" [o1 x b o2])
              (namin-substo b o2 x out)]
             [(not-fno o1)
              (trace-lvars "not-equal" [o1 x b])
              (appo o1 o2 out)]))))]
    [(fresh [e0 o0]
       (nom/fresh [c]
         (lamo c e0 e)
         (lamo c o0 out)
         (nom/hash c a) ;; [new/c]λc.e ≡α λc.e
         (nom/hash c new) ;; [c/a]λc.a ≡α λa.c ≢α λc.c
         (namin-substo e0 new a o0)))]))

(defn namin-eval-expo [exp val]
  (conde
    [(fresh [body]
      (nom/fresh [x]
        (lamo x body exp)
        (trace-lvars "lamo" [x body exp val])
        (== exp val)))]
    [(fresh [rator rand body]
      (nom/fresh [x]
        (appo rator rand exp)
        (trace-lvars "appo" [rator rand])
        (lamo x body rator)
        (namin-substo body rand x val)))]))

(defn substo [exp x v subexp]
  (conde
    [(symbolo exp)
     (== x exp)
     (== v subexp)]
    [(symbolo exp)
     (!= exp x)
     (== exp subexp)]
    [(nom/fresh [arg body subbody]
      (lamo arg body exp)
      (lamo arg subbody subexp)
      (nom/hash arg v)
      (nom/hash arg x)
      (substo body x v subbody))]
    [(fresh [rator rand r1 r2]
      (== `(~rator ~rand) exp)
      (substo rator x v r1)
      (substo rand x v r2)
      (conde
        [(fresh [x body]
          (lamo x body r1)
          (symbolo x)
          (substo body x r2 subexp))]
        [(not-fno r1)
          (== `(~r1 ~r2) subexp)]))]))

(defn eval-expo [exp val]
  (conde
    [(fresh [x body]
      (== `(~'fn [~x] ~body) exp)
      (symbolo x)
      (== exp val))]
    [(fresh [rator rand r1]
      (== `(~rator ~rand) exp)
      (eval-expo rator r1)
      (conde
        [(fresh [x body]
          (== r1 `(~'fn [~x] ~body))
          (symbolo x)
          (substo body x rand val))]
        [(not-fno r1)
         (== exp val)]))]))
