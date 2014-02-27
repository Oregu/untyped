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
      (eval-expo
        (app
          (lam n (lam f (lam x (app f (app (app n f) x)))))
          (lam f1 (lam x1 (app f1 x1)))) q))))

(defn test-plus []
  (run 1 [q]
    (nom/fresh [m n f x f1 x1 f2 x2]
      (eval-expo
        (app
          (app
            (lam m (lam n (lam f (lam x (app (app m f) (app (app n f) x)))))) ; +
            (lam f1 (lam x1 (app f1 (app f1 x1)))))                           ; 2
          (lam f2 (lam x2 (app f2 (app f2 (app f2 x2))))))                    ; 3
        q))))

(defn gen-ch3 []
  (time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
    (app (lam n (lam f (lam x (app f (app (app n f) x))))) q)       ; succ ? =
    (lam f1 (lam x1 (app f1 (app f1 (app f1 (app f1 x1))))))))))))  ; 4

(defn gen-ch5 []
  (time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
    (app (lam n (lam f (lam x (app f (app (app n f) x))))) q)                        ; succ ? =
    (lam f1 (lam x1 (app f1 (app f1 (app f1 (app f1 (app f1 (app f1 x1)))))))))))))) ; 6

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
