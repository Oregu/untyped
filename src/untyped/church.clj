(ns untyped.church
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        [untyped.core]))

(defn ch0 [f x] (lam f (lam x x)))
(defn ch1 [f x] (lam f (lam x (app f x))))
(defn ch2 [f x] (lam f (lam x (app f (app f x)))))
(defn ch3 [f x] (lam f (lam x (app f (app f (app f x))))))
(defn ch4 [f x] (lam f (lam x (app f (app f (app f (app f x)))))))
(defn ch5 [f x] (lam f (lam x (app f (app f (app f (app f (app f x))))))))
(defn ch6 [f x] (lam f (lam x (app f (app f (app f (app f (app f (app f x)))))))))
(defn ch-succ [n f x] (lam n (lam f (lam x (app f (app (app n f) x))))))
(defn ch+ [m n f x] (lam m (lam n (lam f (lam x (app (app m f) (app (app n f) x)))))))

(defn gen-ch3 []    ;; 898 ms
  (time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
    (app (ch-succ n f x) q) ; succ ? =
    (ch4 f1 x1)))))))       ; 4

(defn gen-ch5 []    ;; 36 s
  (time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
    (app (ch-succ n f x) q) ; succ ? =
    (ch6 f1 x1)))))))       ; 6

(defn gen-ch-succ [] ;; 42 s
  (time (first (run 1 [q]
    (nom/fresh [f x f1 x1]
      (eval-expo (app q (ch0 f x)) (ch1 f1 x1)))     ; q? 0 = 1
    (nom/fresh [f x f1 x1]
      (eval-expo (app q (ch1 f x)) (ch2 f1 x1))))))) ; q? 1 = 2

;; NOT working. Overflows
(defn gen-ch+ []
  (time (first (run 1 [q]
    (nom/fresh [f x f1 x1 f2 x2]
      (eval-expo (app (app q (ch2 f x)) (ch3 f1 x1)) ; q? 2 3 =
                 (ch5 f2 x2)))))))                   ; 5

;; Still overflows
(defn gen-ch+-2 []
  (time (first (run 1 [q] (fresh [s2]
    (nom/fresh [f x f1 x1 f2 x2]
      (eval-expo (app q (ch2 f x)) s2)
      (eval-expo (app s2 (ch3 f1 x1)) (ch5 f2 x2))))))))
