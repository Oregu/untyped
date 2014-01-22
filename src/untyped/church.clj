(ns untyped.church
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [untyped.core]))

(def ch0 '(fn [f] (fn [x] x)))
(def ch-succ '(fn [n] (fn [f] (fn [x] (f ((n f) x))))))

(def ch1 (first (run 1 [q] (eval-expo `(~ch-succ ~ch0) q))))
(def ch2 (first (run 1 [q] (eval-expo `(~ch-succ ~ch1) q))))

(def ch+ '(fn [m] (fn [n] (fn [f] (fn [x] ((m f) ((n f) x)))))))
(def ch3 (first (run 1 [q] (eval-expo `((~ch+ ~ch1) ~ch2) q))))
(def ch4 (first (run 1 [q] (eval-expo `((~ch+ ~ch2) ~ch2) q))))
(def ch6 (first (run 1 [q] (eval-expo `((~ch+ ~ch2) ~ch4) q))))

(defn gen-ch3 []
  (time (first (run 1 [q] (eval-expo `(~ch-succ ~q) ch4)))))

(defn gen-ch5 []
  (time (first (run 1 [q] (eval-expo `(~ch-succ ~q) ch6)))))

(defn gen-ch-succ []
  (time (doall (run 2 [q] (eval-expo `(~q ~ch3) ch4)))))

(defn gen-ch-succ-strict []
  (time (doall (run 1 [q]
                    (eval-expo `(~q ~ch1) ch2)
                    (eval-expo `(~q ~ch3) ch4)))))

(defn gen-eater []
  (time (first (run 1 [q] (eval-expo `(~q ~'x) q)))))
