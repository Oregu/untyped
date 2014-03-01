(ns untyped.test.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic :exclude [is]]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        untyped.core
        clojure.test))

(deftest eval-forward
  (is (first
    (run 1 [q]
      (nom/fresh [x a]
        (eval-expo (app (lam x (app x x)) (lam a a)) q)
        (lamo a a q))))))

(deftest eval-backward-id
  (is (first
    (run 1 [q] (nom/fresh [a b]
      (eval-expo (app q (lam a a))
                 (lam b b)))))))

(defn ch0 [f x] (lam f (lam x x)))
(defn ch-succ [n f x] (lam n (lam f (lam x (app f (app (app n f) x))))))

(defn ch1 [f x] (lam f (lam x (app f x))))
(defn ch2 [f x] (lam f (lam x (app f (app f x)))))

(deftest eval-succ
  (is (first (run 1 [q] (nom/fresh [n f x f1 x1]
    (eval-expo (app (ch-succ n f x) (ch0 f x))
               (ch1 f1 x1))))))
  (is (first (run 1 [q] (nom/fresh [n f x f1 x1]
    (eval-expo (app (ch-succ n f x) (ch1 f1 x1))
               (ch2 f x)))))))

(defn ch+ [m n f x] (lam m (lam n (lam f (lam x (app (app m f) (app (app n f) x)))))))

(defn ch3 [f x] (lam f (lam x (app f (app f (app f x))))))
(defn ch4 [f x] (lam f (lam x (app f (app f (app f (app f x)))))))
(defn ch5 [f x] (lam f (lam x (app f (app f (app f (app f (app f x))))))))

(deftest eval-plus
  (is (first (run 1 [q] (nom/fresh [m n f x f1 x1 f2 x3]
    (eval-expo (app (app (ch+ m n f x) (ch2 f2 x1)) (ch1 f1 x1)) (ch3 f x3))))))
  (is (first (run 1 [q] (nom/fresh [m n f x]
    (eval-expo (app (app (ch+ m n f x) (ch3 f x)) (ch2 f x)) (ch5 f x)))))))
