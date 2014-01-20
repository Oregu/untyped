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

(defn lookupo [x env t]
  (conde
    [(emptyo env) (== x t)]
    [(fresh [rest y v]
      (conso `(~y ~v) rest env)
      (conde
        [(== y x) (== v t)]
        [(!= y x) (lookupo x rest t)]))]))

(defn eval-expo [exp env val]
  (conde
    [(symbolo exp)
     (lookupo exp env val)]
    [(fresh [rator rand r2 x body env+]
      (== `(~rator ~rand) exp)
      (eval-expo rator env `(~'fn [~x] ~body))
      (symbolo x)
      (eval-expo rand env r2)
      (conso `(~x ~r2) env env+)
      (eval-expo body env+ val))]
    [(fresh [rator rand r1 r2 h t]
      (== `(~rator ~rand) exp)      
      (conso h t env)
      (eval-expo rator env r1)
      (not-fno r1)
      (eval-expo rand env r2)
      (== `(~r1 ~r2) val))]
    [(fresh [rator rand r1]
      (== `(~rator ~rand) exp)
      (emptyo env)
      (eval-expo rator env r1)
      (not-fno r1)
      (== exp val))]
    [(fresh [x body body2]
      (== `(~'fn [~x] ~body) exp)
      (symbolo x)
      (eval-expo body env body2)
      (== `(~'fn [~x] ~body2) val))]))
