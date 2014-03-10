(ns untyped.combi
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        [untyped.core]))

(defn S [x y z] (lam x (lam y (lam z (app (app x z) (app y z))))))
(defn K [x y]   (lam x (lam y x)))
(defn I [x]     (lam x x))

(defn gen-ex2-9i []
  (time (first (run 1 [q] (nom/fresh [x a]
    (eval-expo (app q x) (app x (I a))))))))

(defn gen-ex2-9ii []
  (time (first (run 1 [q] (nom/fresh [x y a]
    (eval-expo (app (app q x) y) (app (app x (I a)) y)))))))

; (defn gen-2-10iii []
;   (time (first (run 1 [q] (nom/fresh [x y a]
;     (eval-expo (app (app q (I x)) y) (app (app x (I a)) y)))))))

(defn gen-eater [] ;; Also called K-infinity
  (time (first (run 1 [q] (nom/fresh [x]
    (eval-expo (app q x) q))))))
