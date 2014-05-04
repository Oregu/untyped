(ns untyped.combi
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        [untyped.core]))

(defn S [x y z] (lam x (lam y (lam z (app (app x z) (app y z))))))
(defn K [x y]   (lam x (lam y x)))
(defn I [x]     (lam x x))

(defn Dbl [e] (app e e))
(defn W [f x] (lam x (app f (app x x))))
(defn Y [f x] (lam f (Dbl (W f x))))

;; Exercices from “Introduction to Lambda Calculus”
(defn gen-ex2-9i []
  (time (first (run 1 [q] (nom/fresh [x a]
    (eval-expo (app q x) (app x (I a))))))))

(defn gen-ex2-9ii []
  (time (first (run 1 [q] (nom/fresh [x y a]
    (eval-expo (app (app q x) y) (app (app x (I a)) y)))))))

; (defn gen-2-10iii []
;   (time (first (run 1 [q] (nom/fresh [x y a]
;     (eval-expo (app (app q (I x)) y) (app (app x (I a)) y)))))))

(defn gen-quines []
  (time (doall (run 2 [q]
    (eval-expo q q)))))

; In progress. Will's version for Petite Chez computes it for 7 minutes
; Not sure if mine is working at all!
(defn gen-Y []
  (time (doall (run 1 [Y] (fresh [U]
    (nom/fresh [f z]
      (lamo f (app U U) Y)
      (nom/hash z Y)
      (eval-expo (app Y z) (app z (app Y z)))))))))

(defn gen-Y-W []
  (time (doall (run 1 [Y] (fresh [U]
    (nom/fresh [f z]
      (lamo f (app U U) Y)
      (nom/hash z Y)
      (step-equalo (app Y z) (app z (app Y z)))))))))

(defn gen-eater-hinted-W []
  (time (doall (run 1 [B] (fresh [U]
    (nom/fresh [f z]
      (lamo f (app U U) B)
      (nom/hash z B)
      (step-equalo (app B z) B)))))))

(defn gen-eater-W []
  (time (doall (run 1 [B] (nom/fresh [z]
      (nom/hash z B)
      (step-equalo `(~'app ~B (~'var ~z)) B))))))

;; K-infinity
;; a.k.a Hopelessly Egocentric Bird B or E, such that Bx=B or Ex=E
(defn gen-eater []
  (time (doall (run 1 [B] (nom/fresh [x y] (fresh [U]
    (lamo y (app U U) B)
    (eval-expo (app B x) B)))))))
