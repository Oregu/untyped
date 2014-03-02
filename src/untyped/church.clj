(ns untyped.church
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        [untyped.core]))

(defn ch0 [f x] (lam f (lam x x)))
(defn ch1 [f x] (lam f (lam x (app f x))))
(defn ch4 [f x] (lam f (lam x (app f (app f (app f (app f x)))))))
(defn ch6 [f x] (lam f (lam x (app f (app f (app f (app f (app f (app f x)))))))))
(defn ch-succ [n f x] (lam n (lam f (lam x (app f (app (app n f) x))))))

(defn gen-ch3 []
  (time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
    (app (ch-succ n f x) q) ; succ ? =
    (ch4 f1 x1)))))))       ; 4

(defn gen-ch5 []
  (time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
    (app (ch-succ n f x) q) ; succ ? =
    (ch6 f1 x1)))))))       ; 6

(defn gen-ch-succ []
  (time (first (run 1 [q]
  (nom/fresh [f x f1 x1]
    (eval-expo (app q (lam f (lam x x))) (lam f1 (lam x1 (app f1 x1)))))      ; ? 0 = 1
  (nom/fresh [f x f1 x1]
    (eval-expo (app q (lam f (lam x (app f x)))) (lam f1 (lam x1 (app f1 (app f1 x1))))))      ; ? 1 = 2
  (nom/fresh [f x f1 x1]
    (eval-expo (app q (lam f (lam x (app f (app f x))))) (lam f1 (lam x1 (app f1 (app f1 (app f1 x1))))))))))) ; ? 2 = 3

(defn gen-eater [] ;; Also called K-infinity
  (time (first (run 1 [q] (nom/fresh [x] (eval-expo (app q x) q))))))
