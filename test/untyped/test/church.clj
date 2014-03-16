(ns untyped.test.church
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic :exclude [is]]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        untyped.core
        untyped.church
        clojure.test))

(deftest eval-succ
  (is (first (run 1 [q] (nom/fresh [n f x f1 x1]
    (eval-expo (app (ch-succ n f x) (ch 0 f x))
               (ch 1 f1 x1))))))
  (is (first (run 1 [q] (nom/fresh [n f x f1 x1]
    (eval-expo (app (ch-succ n f x) (ch 1 f1 x1))
               (ch 2 f x)))))))

(deftest eval-plus
  (is (first (run 1 [q] (nom/fresh [m n f x f1 x1 f2 x3]
    (eval-expo (app (app (ch+ m n f x) (ch 2 f2 x1)) (ch 1 f1 x1)) (ch 3 f x3))))))
  (is (first (run 1 [q] (nom/fresh [m n f x]
    (eval-expo (app (app (ch+ m n f x) (ch 3 f x)) (ch 2 f x)) (ch 5 f x)))))))
