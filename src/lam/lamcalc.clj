(ns lam.lamcalc
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [lam.core]))

(def lam-zero '(fn [f] (fn [x] x)))
(def lam-succ '(fn [n] (fn [f] (fn [x] (f ((n f) x))))))

(def lam-one (first (run 1 [q] (eval-expo `(~lam-succ ~lam-zero) q))))
(def lam-two (first (run 1 [q] (eval-expo `(~lam-succ ~lam-one) q))))
