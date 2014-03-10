(ns untyped.test.combi
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic :exclude [is]]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        untyped.core
        untyped.combi
        clojure.test))

(deftest eval-SKK
  (is (first (run 1 [q] (nom/fresh [x y xx yy a b c z]
    (eval-expo
      (app (app (app (S a b c) (K x y)) (K xx yy)) z)
      z))))))
