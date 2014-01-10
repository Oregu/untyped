(ns lam.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]))

(defn symbolo [x] (predc x symbol?))
(defn not-fno [x] (predc x (fn [x] (not (and (seq? x) (= (first x) 'fn))))))

(declare substo)

(defn eval-expo [exp val]
  (conde
    [(symbolo exp)
     (== val exp)]
    [(fresh [rator rand r2 x body body2]
      (== `(~rator ~rand) exp)
      (eval-expo rator `(~'fn [~x] ~body))
      (eval-expo rand r2)
      (substo body x r2 body2)
      (eval-expo body2 val))]
    [(fresh [rator rand r1 r2]
      (== `(~rator ~rand) exp)
      (not-fno rator)
      (eval-expo rator r1)
      (eval-expo rand r2)
      (== val `(~r1 ~r2)))]
    [(fresh [x body body-ex]
      (== `(~'fn [~x] ~body) exp)
      (symbolo x)
      (trace-lvars "lam" [x body])
      (eval-expo body body-ex)
      (== `(~'fn [~x] ~body-ex) val))]))

(defn substo [exp vr vl body-ex]
  (conde
    [(symbolo exp)
     (== exp vr)
     (== vl body-ex)]
    [(symbolo exp)
     (!= exp vr)
     (== exp body-ex)]
    [(fresh [rator rand rator-ex rand-ex]
      (== `(~rator ~rand) exp)
      (substo rator vr vl rator-ex)
      (substo rand vr vl rand-ex)
      (eval-expo `(~rator-ex ~rand-ex) body-ex))]
    [(fresh [x body body2-ex]
      (== `(~'fn [~x] ~body) exp)
      (!= x vr)
      (substo body vr vl body2-ex)
      (== body-ex `(~'fn [~x] ~body2-ex)))]
    [(fresh [x body]
      (== `(~'fn [~x] ~body) exp)
      (== x vr)
      (== exp body-ex))]))
