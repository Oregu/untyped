(ns untyped.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]))

(defn not-fn? [x]
  (or (not (coll? x))
      (empty? x)
      (not= (first x) 'fn)
      (not (vector? (second x)))))

(defn not-fno [x] (predc x not-fn?))
(defn symbolo [x] (predc x symbol?))

(defn excludo [x l o]
  (conde
    [(emptyo l)(emptyo o)]
    [(fresh [h t t1]
      (conso h t l)
      (!= h x)
      (excludo x t t1)
      (conso h t1 o))]
    [(fresh [t]
      (conso x t l)
      (excludo x t o))]))

(defn not-in-listo [x l]
  (conde
    [(emptyo l)]
    [(fresh [h t]
      (conso h t l)
      (!= h x)
      (not-in-listo x t))]))

(defn free-varo [exp fv]
  (conde
    [(symbolo exp)
     (conso exp '() fv)]
    [(fresh [x body fv-f]
      (== `(~'fn [~x] ~body) exp)
      (free-varo body fv-f)
      (excludo x fv-f fv))]
    [(fresh [rator rand fv1 fv2]
      (== `(~rator ~rand) exp)
      (free-varo rator fv1)
      (free-varo rand fv2)
      (appendo fv1 fv2 fv))]))

(defn substo [exp x v subexp]
  (conde
    [(symbolo exp)
     (== x exp)
     (== v subexp)]
    [(symbolo exp)
     (!= exp x)
     (== exp subexp)]
    [(fresh [arg body subbody fv]
      (== `(~'fn [~arg] ~body) exp)
      ; (free-varo v fv)
      ; (not-in-listo arg fv)
      (substo body x v subbody)
      (== `(~'fn [~arg] ~subbody) subexp))]
    [(fresh [rator rand r1 r2]
      (== `(~rator ~rand) exp)
      (substo rator x v r1)
      (substo rand x v r2)
      (conde
        [(fresh [x body]
          (== r1 `(~'fn [~x] ~body))
          (substo body x r2 subexp))]
        [(not-fno r1)
          (== `(~r1 ~r2) subexp)]))]))

(defn eval-expo [exp val]
  (conde
    [(symbolo exp)
     (== exp val)]
    [(fresh [x body]
      (== `(~'fn [~x] ~body) exp)
      (symbolo x)
      (== exp val))]
    [(fresh [rator rand r1]
      (== `(~rator ~rand) exp)
      (eval-expo rator r1)
      (not-fno r1)
      (== exp val))]
    [(fresh [rator rand x body]
      (== `(~rator ~rand) exp)
      (eval-expo rator `(~'fn [~x] ~body))
      (symbolo x)
      (substo body x rand val))]))
