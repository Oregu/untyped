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

(deftest eval-backward
  (is (first
    (run 1 [q] (nom/fresh [a b]
      (eval-expo (app q (lam a a))
                 (lam b b)))))))
