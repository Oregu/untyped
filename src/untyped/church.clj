(ns untyped.church
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        [untyped.core]))

(defn ch [n f x]
  "Produce Church numeral n with noms f and x"
  (lam f (lam x (nth (iterate (partial app f) x) n))))

(defn ch-succ [n f x] (lam n (lam f (lam x (app f (app (app n f) x))))))
(defn ch+ [m n f x] (lam m (lam n (lam f (lam x (app (app m f) (app (app n f) x)))))))

(defn gen-ch3 []    ;; 785 ms
  (time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
    (app (ch-succ n f x) q) ; succ q? =
    (ch 4 f1 x1)))))))      ; 4

(defn gen-ch5 []    ;; 31 s
  (time (first (run 1 [q] (nom/fresh [n f x f1 x1] (eval-expo
    (app (ch-succ n f x) q) ; succ q? =
    (ch 6 f1 x1)))))))      ; 6

(defn gen-ch-succ [] ;; 41 s
  (time (first (run 1 [q]
    (fresh [r1 r2]
     (nom/fresh [n f x f1 x1]

      ; We can legitimately hint miniKanren because type is easily derivable.
      ; Increases search from 41s to 7s.
      (== q (lam n (lam f (lam x (app r1 r2)))))

      (eval-expo (app q (ch 0 f x)) (ch 1 f1 x1))        ; q? 0 = 1
      (eval-expo (app q (ch 1 f x)) (ch 2 f1 x1))))))))  ; q? 1 = 2

;; NOT working. Out of memory after 15 mins.
;; With 2 gigs tried to compute for 2 hours.
;; No luck.
(defn gen-ch+ []
  (time (first (run 1 [q]
    (fresh [r1 r2 r3 r4 r5]
      (nom/fresh [m n f x f1 x1 f2 x2]
        ; With this hint runs for 5s.
        (== q (lam m (lam n (lam f (lam x (app (app r1 r2) (app (app r3 r2) r5)))))))
        (eval-expo (app (app q (ch 0 f x)) (ch 0 f1 x1)) ; q? 0 0 =
                   (ch 0 f2 x2))                         ; 0
        (eval-expo (app (app q (ch 0 f x)) (ch 1 f1 x1)) ; q? 0 1 =
                   (ch 1 f2 x2))                         ; 1
        (eval-expo (app (app q (ch 1 f x)) (ch 0 f1 x1)) ; q? 1 0 =
                   (ch 1 f2 x2))                         ; 1
        (eval-expo (app (app q (ch 1 f x)) (ch 1 f1 x1)) ; q? 1 1 =
                   (ch 2 f2 x2))))))))                   ; 2
