(ns untyped.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]))

(defn not-fn? [x]
  (or (not (coll? x))
      (empty? x)
      (not= (first x) 'fn)
      (not (vector? (second x)))))

(defn not-fno [x] (predc x not-fn? 'not-fn?))
(defn symbolo [x] (predc x symbol? 'symbol?))

(defn lam  [x e] `(~'fn ~(nom/tie x e)))
(defn lamo [x e o] (== o (lam x e)))

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
