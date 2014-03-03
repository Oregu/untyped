(ns untyped.combi
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        [untyped.core]))

(defn S [x y z] (lam x (lam y (lam z (app (app x z) (app y z))))))
(defn K [x y] (lam x (lam y x)))

(defn gen-eater [] ;; Also called K-infinity
  (time (first (run 1 [q] (nom/fresh [x]
    (eval-expo (app q x) q))))))
