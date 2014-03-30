(ns untyped.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]))

(defn not-fn? [x]
  (or (not (coll? x))
      (empty? x)
      (not= (first x) 'fn)))

(defn not-fno [x] (predc x not-fn? 'not-fn?))
(defn symbolo [x] (predc x symbol? 'symbol?))
(defn nomo    [x] (predc x nom? 'nom?))

(defn lam [x e] `(~'fn ~(nom/tie x e)))
(defn app [e1 e2] `(~e1 ~e2))
(defn lamo [x e out] (== out (lam x e)))
(defn appo [e1 e2 out] (all (== out (app e1 e2)) (!= e1 'fn)))

(defn valo [e]
  (fresh [b]
    (nom/fresh [x]
      (lamo x b e))))

(defn substo [e new a out] ;; out == [new/a]e
  (conde
    [(nomo e) (== e a) (== new out)]
    [(nomo e) (!= e a) (== e out)]
    [(fresh [e1 e2 o1 o2 b]
       (appo e1 e2 e)
       (substo e1 new a o1)
       (substo e2 new a o2)
       (nom/fresh [x]
        (conde
          [(lamo x b o1)
           (substo b o2 x out)]
          [(not-fno o1)
           (appo o1 o2 out)])))]
    [(fresh [e0 o0]
       (nom/fresh [c]
         (lamo c e0 e)
         (lamo c o0 out)
         (nom/hash c a) ;; [new/c]λc.e ≡α λc.e
         (nom/hash c new) ;; [c/a]λc.a ≡α λa.c ≢α λc.c
         (substo e0 new a o0)))]))

(defn lookupo [x env v]
  (conde
    [(emptyo env)
     (== x v)]
    [(fresh [t]
      (conso `(~x ~v) t env))]
    [(fresh [h hv t]
      (conso `(~h ~hv) t env)
      (!= x h)
      (lookupo x t v))]))

(defn eval-expo
  ([exp val] (eval-expo exp '() val))
  ([exp env val]
  (conde
    [(nomo exp) (lookupo exp env val)]
    [(fresh [b b2]
      (nom/fresh [x]
        (lamo x b exp)
        (eval-expo b env b2)
        (lamo x b2 val)))]
    [(fresh [rator rand r1 r2]
      (appo rator rand exp)
      (eval-expo rator env r1)
      (conde
        [(fresh [body env+]
          (nom/fresh [x]
            (lamo x body r1)
            (eval-expo rand env r2)
            (conso `(~x ~r2) env env+)
            (eval-expo body env+ val)))]
        [(not-fno r1)
         (conde
           [(emptyo env)
            (== exp val)]
           [(fresh [h t]
              (conso h t env)
              (eval-expo rand env r2)
              (appo r1 r2 val))])]))])))

#_(defn eval-expo [exp val]
  (conde
    [(valo exp) (== exp val)]
    [(fresh [rator rand r1 body]
      (nom/fresh [x]
        (appo rator rand exp)
        (lamo x body r1)
        (eval-expo rator r1)
        (substo body rand x val)))]))
