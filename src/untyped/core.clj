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

(defn substo [x env v]
  (conde
    [(emptyo env)
     (== x v)]
    [(fresh [t]
      (conso `(~x ~v) t env))]
    [(fresh [h hv t]
      (conso `(~h ~hv) t env)
      (!= x h)
      (substo x t v))]))

(defn eval-expo [exp env val]
  (conde
    [(symbolo exp)
     (substo exp env val)]
    [(fresh [x body]
      (== `(~'fn [~x] ~body) exp)
      (symbolo x)
      (conde
        [(emptyo env)
         (== exp val)]
        [(fresh [h t b2]
          (conso h t env)
          (eval-expo body env b2)
          (== `(~'fn [~x] ~b2) val))]))]
    [(fresh [rator rand r1 r2]
      (== `(~rator ~rand) exp)
      (eval-expo rator env r1)
      (conde
        [(fresh [x body env+]
          (== r1 `(~'fn [~x] ~body))
          (symbolo x)
          (eval-expo rand env r2)
          (conso `(~x ~r2) env env+)
          (eval-expo body env+ val))]
        [(not-fno r1)
         (conde
           [(emptyo env)
            (== exp val)]
           [(fresh [h t]
              (conso h t env)
              (eval-expo rand env r2)
              (== `(~r1 ~r2) val))])]))]))
