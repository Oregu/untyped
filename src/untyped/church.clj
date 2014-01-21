(ns untyped.church
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [untyped.core]))

(def ch0 '(fn [f] (fn [x] x)))
(def ch-succ '(fn [n] (fn [f] (fn [x] (f ((n f) x))))))

(def ch1 (first (run 1 [q] (eval-expo `(~ch-succ ~ch0) '() q))))
(def ch2 (first (run 1 [q] (eval-expo `(~ch-succ ~ch1) '() q))))

(def ch+ '(fn [m] (fn [n] (fn [f] (fn [x] ((m f) ((n f) x)))))))
(def ch4 (first (run 1 [q] (eval-expo `((~ch+ ~ch2) ~ch2) '() q))))

(defn count-ch-three []
  (time (first (run 1 [q] (eval-expo `(~ch-succ ~q) '() ch4)))))

