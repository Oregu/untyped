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

(defn substo [exp x v subexp]
  (conde
    [(symbolo exp)
     (== x exp)
     (== v subexp)]
    [(symbolo exp)
     (!= exp x)
     (== exp subexp)]
    [(fresh [arg body subbody]
      (== `(~'fn [~arg] ~body) exp)
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
      (conde
        [(fresh [x body]
          (== r1 `(~'fn [~x] ~body))
          (symbolo x)
          (substo body x rand val))]
        [(not-fno r1)
         (== exp val)]))]))
