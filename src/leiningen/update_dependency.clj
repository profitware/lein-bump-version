(ns leiningen.update-dependency
  (:require [rewrite-clj.zip :as z]))

(defn update-dependency
  "Updates a project in :dependencies to a particular version."
  [project dep version]
  (let [f "project.clj"]
    (some-> (z/of-file f)
            (z/find-value z/next 'defproject)
            (z/find-value :dependencies)
            z/right
            (z/find-next-depth-first #(= (str (z/sexpr (z/down %))) dep))
            (z/edit (constantly [(symbol dep) version]))
            z/root
            ((fn [x] (spit f x))))))
