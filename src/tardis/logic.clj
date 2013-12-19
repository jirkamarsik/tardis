(ns tardis.logic
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]))

(defne undo-double-negationo [pre post]
  ([['not ['not post]]
    post]))

(defne pass-neg-through-exo [pre post]
  ([['not ['exists ['lambda [var] body]]]
    ['forall ['lambda [var] ['not body]]]]))

(defne make-implicationo [pre post]
  ([['not [['and A] ['not B]]]
    [['impl A] B]])
  ([['not [['and A] [['and B] C]]]
    post]
     (make-implicationo ['not [['and [['and A] B]] C]] post)))

(defn use-forall-in-everyo [pre post]
  (fresh [step var body body']
         (pass-neg-through-exo pre step)
         (== step ['forall ['lambda [var] body]])
         (== post ['forall ['lambda [var] body']])
         (conde [(make-implicationo body body')]
                [(use-forall-in-everyo body body')])))

(defne introduce-binderso [pre post]
  ([[quantifier ['lambda [var] body]]
    [quantifier [var] body]]
     (membero quantifier ['exists 'forall])))

(defne uncurryo [pre post]
  ([[[F X] Y . Z] [F X Y . Z]]))

(defn -all [step-rel]
  (letfn [(step-rel-all [pre post]
            (conde [(step-rel pre post)]
                   [(step-rel-list pre post)]))
          (step-rel-list [pre post]
            (fresh [pre-head pre-tail post-head post-tail]
                   (== pre (lcons pre-head pre-tail))
                   (== post (lcons post-head post-tail))
                   (conde [(step-rel-all pre-head post-head)
                           (== pre-tail post-tail)]
                          [(step-rel-list pre-tail post-tail)
                           (== pre-head post-head)])))]
    step-rel-all))

(defn fix [f]
  (fn fix-f [x]
    (let [fx (f x)]
      (if (= x fx)
        x
        (fix-f fx)))))

(defn -* [step-rel]
  (letfn [(step-fn [in]
            (or (first (run 1 [out] (step-rel in out)))
                in))]
    (fix step-fn)))

(def clean
  (comp (-> uncurryo -all -*)
        (-> introduce-binderso -all -*)
        (-> use-forall-in-everyo -all -*)
        (-> undo-double-negationo -all -*)))


(def render-hiero (-> (make-hierarchy)
                      (derive 'and     :binary-op)
                      (derive 'or      :binary-op)
                      (derive 'impl    :binary-op)
                      (derive '=       :binary-op)
                      (derive 'part-of :binary-op)
                      (derive 'not     :unary-op)
                      (derive 'lambda  :binder)
                      (derive 'exists  :binder)
                      (derive 'forall  :binder)))

(def symbol-table {'and     "∧"
                   'or      "∨"
                   'impl    "→"
                   '=       "="
                   'part-of "⊆"
                   'not     "¬"
                   'lambda  "λ"
                   'exists  "∃"
                   'forall  "∀"})

(defmulti render
  (fn [form]
    (if (coll? form)
      (first form)
      :var))
  :hierarchy #'render-hiero)

(defmethod render :var [v]
  (name v))

(defmethod render :binary-op [[op x y]]
  (str "(" (render x) " " (symbol-table op) " " (render y) ")"))

(defmethod render :unary-op [[op x]]
  (str "(" (symbol-table op) (render x) ")"))

(defmethod render :binder [[binder [var] body]]
  (str (symbol-table binder) (render var) "." "(" (render body) ")"))

(defmethod render :default [[pred & args]]
  (str (render pred) "(" (apply str (interpose "," (map render args))) ")"))


(def ^:sonic-screwdriver simplify-sexp-formula
  "Simplifies an s-expression-encoded logical formula by removing double
  negations, introducing restricted universal quantification (∀x. Px → Qx),
  treating quantifiers as binders and uncurrying applications."
  (comp render clean read-string))
