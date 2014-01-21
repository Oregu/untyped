(ns untyped.test.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic :exclude [is]]
        untyped.core
        clojure.test))

(deftest eval-forward
  (is (= '(fn [a] a)
         (first
           (run 1 [q]
             (eval-expo '((fn [x] (x x)) (fn [a] a)) q))))))

(deftest eval-backward-id
  (is (= '(fn [_0] _0)
         (first (first
            (run 1 [q]
              (fresh [a] (eval-expo `(~q (~'fn [~'a] ~'a))
                                    '(fn [a] a)))))))))

(def ch0 '(fn [f] (fn [x] x)))
(def ch-succ '(fn [n] (fn [f] (fn [x] (f ((n f) x))))))

(def ch1 '(fn [f] (fn [x] (f x))))
(def ch2 '(fn [f] (fn [x] (f (f x)))))

(deftest eval-succ
  (is (= ch1
         (first (run 1 [q] (eval-expo `(~ch-succ ~ch0) q)))))
  (is (= ch2
         (first (run 1 [q] (eval-expo `(~ch-succ ~ch1) q))))))

(def ch+ '(fn [m] (fn [n] (fn [f] (fn [x] ((m f) ((n f) x)))))))

(def ch3 '(fn [f] (fn [x] (f (f (f x))))))
(def ch4  '(fn [f] (fn [x] (f (f (f (f x)))))))
(def ch5  '(fn [f] (fn [x] (f (f (f (f (f x))))))))

(deftest eval-plus
  (is (= ch3
         (first (run 1 [q] (eval-expo `((~ch+ ~ch2) ~ch1) q)))))
  (is (= ch5
         (first (run 1 [q] (eval-expo `((~ch+ ~ch3) ~ch2) q))))))
