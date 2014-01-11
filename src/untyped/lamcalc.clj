(ns untyped.lamcalc
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [untyped.core]))

(def ch-zero '(fn [f] (fn [x] x)))
(def ch-succ '(fn [n] (fn [f] (fn [x] (f ((n f) x))))))

(def ch-one (first (run 1 [q] (eval-expo `(~ch-succ ~ch-zero) '() q))))
(def ch-two (first (run 1 [q] (eval-expo `(~ch-succ ~ch-one) '() q))))

(def ch-plus '(fn [m] (fn [n] (fn [f] (fn [x] ((m f) ((n f) x)))))))
(def ch-four (first (run 1 [q] (eval-expo `((~ch-plus ~ch-two) ~ch-two) '() q))))
