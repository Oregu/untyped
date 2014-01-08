(ns lam.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]))

(defn noo [tag u] (predc u (fn [x] (clojure.core/not= (if (seq? x) (first x) x) tag))))
(defn symbolo [x] (predc x symbol?))
(defn not-symbolo [x] (predc x #(not (symbol? %))))
(defn numbero [x] (predc x number?))

(declare not-in-envo)
(declare proper-listo)

(defn lookupo [x env t]
  (fresh [rest y v]
    (conso `(~y ~v) rest env)
    (conde
      [(== y x) (== v t)]
      [(!= y x) (lookupo x rest t)])))

(declare eval-expo)

(defn substo [body vr vl body-ex]
  (conde
    [(symbolo body)
     (== body vr)
     (== body-ex vl)]
    [(symbolo body)
     (!= body vr)
     (== body body-ex)]
    [(numbero body)
     (== body body-ex)]
    [(fresh [rator rand rator-ex rand-ex]
      (== `(~rator ~rand) body)
      (substo rator vr vl rator-ex)
      (substo rand vr vl rand-ex)
      (eval-expo `(~rator-ex ~rand-ex) '() body-ex))]
    [(fresh [x body2 body2-ex]
      (== `(~'fn [~x] ~body2) body)
      (!= x vr)
      (substo body2 vr vl body2-ex) ;; TODO do not substitute bound variables
      (== body-ex `(~'fn [~x] ~body2-ex)))]))

(defn reduceo [body env body-ex] ;; TODO trim environment
  (conde
    [(emptyo env)
     (== body body-ex)]
    [(fresh [vr vl env-t body2]
      (conso `(~vr ~vl) env-t env)
      (substo body vr vl body2)
      (reduceo body2 env-t body-ex))]))

(defn eval-expo [exp env val]
  (conde
    [(fresh [v]
      (== `(~'quote ~v) exp)
      (not-in-envo 'quote env)
      (noo 'closure v)
      (== v val))]
    [(fresh [a*]
      (conso 'list a* exp)
      (not-in-envo 'list env)
      (noo 'closure a*)
      (proper-listo a* env val))]
    [(symbolo exp)
      (lookupo exp env val)]
    [(symbolo exp)
      (lookupo exp `((~exp ~exp)) val)]
    [(fresh [rator rand x body env- a env2]
      (== `(~rator ~rand) exp)
      (not-symbolo rator)
      (eval-expo rator env `(~'closure ~x ~body ~env-))
      (eval-expo rand env a)
      (conso `(~x ~a) env- env2)
      (eval-expo body env2 val))]
    [(fresh [rator rand x v2]
      (== `(~rator ~rand) exp)
      (not-symbolo rator)
      (eval-expo rator env v2)
      (noo 'closure v2)
      (reduceo exp env val))]
    [(fresh [x body body-ex env2]
      (== `(~'fn [~x] ~body) exp)
      (symbolo x)
      (not-in-envo 'fn env)
      (reduceo body env body-ex)
      (== `(~'closure ~x ~body-ex ~env) val))]
    [(fresh [rator rand]
      (== `(~rator ~rand) exp)
      (symbolo rator)
      (reduceo exp env val))]
    [(fresh [x body env2]
      (== `(~'closure ~x ~body ~env2) exp)
      (symbolo x)
      (== exp val))]))

(defn not-in-envo [x env]
  (conde
    [(fresh [y v rest]
      (conso `(~y ~v) rest env)
      (!= y x)
      (not-in-envo x rest))]
    [(== '() env)]))

(defn proper-listo [exp env val]
  (conde
    [(== '() exp)
     (== '() val)]
    [(fresh [a d t-a t-d]
       (conso a d exp)
       (conso t-a t-d val)
       (eval-expo a env t-a)
       (proper-listo d env t-d))]))
