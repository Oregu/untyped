(ns untyped.church
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        [untyped.core]))

(def ch0 '(fn [f] (fn [x] x)))
(def ch-succ '(fn [n] (fn [f] (fn [x] (f ((n f) x))))))

(defn test-succ []
  (run 1 [q]
    (nom/fresh [n f x f1 x1]
      (namin-eval-expo
        (app
          (lam n (lam f (lam x (app f (app (app n f) x)))))
          (lam f1 (lam x1 (app f1 x1)))) q))))

(def ch1 (first (run 1 [q] (eval-expo `(~ch-succ ~ch0) q))))
(def ch2 (first (run 1 [q] (eval-expo `(~ch-succ ~ch1) q))))

(def ch+ '(fn [m] (fn [n] (fn [f] (fn [x] ((m f) ((n f) x)))))))
(def ch3 (first (run 1 [q] (eval-expo `((~ch+ ~ch1) ~ch2) q))))
(def ch4 (first (run 1 [q] (eval-expo `((~ch+ ~ch2) ~ch2) q))))
(def ch6 (first (run 1 [q] (eval-expo `((~ch+ ~ch2) ~ch4) q))))

(defn test-plus []
  (run 1 [q]
    (nom/fresh [m n f x f1 x1 f2 x2]
      (namin-eval-expo
        (app
          (app
            (lam n (lam m (lam f (lam x (app (app m f) (app (app n f) x)))))) ; +
            (lam f1 (lam x1 (app f1 (app f1 x1))))) ; 2
          (lam f2 (lam x2 (app f2 (app f2 (app f2 x2)))))) q)))) ; 3

(defn gen-ch3 []
  (time (first (run 1 [q] (eval-expo `(~ch-succ ~q) ch4)))))

(defn gen-ch5 []
  (time (first (run 1 [q] (eval-expo `(~ch-succ ~q) ch6)))))

(defn gen-ch-succ []
  (time (doall (run 1 [q] (eval-expo `(~q ~ch3) ch4)))))

(defn gen-ch-succ-strict []
  (time (doall (run 1 [q]
                    (eval-expo `(~q ~ch0) ch1)
                    (eval-expo `(~q ~ch1) ch2)
                    (eval-expo `(~q ~ch2) ch3)
                    (eval-expo `(~q ~ch3) ch4)))))

(defn gen-eater [] ;; Also called K-infinity
  (time (first (run 1 [q] (eval-expo `(~q ~'x) q)))))
