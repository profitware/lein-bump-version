(ns leiningen.bump-version
  (:require [clojure.string :as s]
            [rewrite-clj.zip :as z]))

(defn bump-version
  ([project]
   (bump-version project nil "project.clj"))
  ([project version]
   (bump-version project version "project.clj"))
  ([project version project-file]
   (let [project-root (z/of-file project-file)
         project-name-token (some-> project-root
                                    (z/find-next-depth-first #(= (-> %
                                                                     z/prev
                                                                     z/sexpr)
                                                                 'defproject)))
         project-group (-> project-name-token
                           z/sexpr
                           str
                           (s/split #"/")
                           first)
         project-old-version (-> project-name-token
                                 z/next
                                 z/sexpr
                                 str)
         bumped-version (or version
                            (let [splitted (s/split project-old-version #"\.")
                                  updated (update splitted 2 #(-> (Integer. %)
                                                                  inc
                                                                  str))]
                              (s/join "." updated)))
         base-token (or (some-> project-name-token
                                z/next
                                (z/edit (constantly bumped-version)))
                        project-name-token)
         dependencies-root (some-> base-token
                                   (z/find-value :dependencies)
                                   z/right)
         bumped-tree (loop [root dependencies-root]
                       (let [edited (some-> root
                                            (z/find-next-depth-first #(and (= (-> %
                                                                                  z/sexpr
                                                                                  str
                                                                                  (s/split #"/")
                                                                                  first)
                                                                              project-group)
                                                                           (= (-> %
                                                                                  z/next
                                                                                  z/sexpr
                                                                                  str)
                                                                              project-old-version)))
                                            z/next
                                            (z/edit (constantly bumped-version))
                                            z/up)
                             right (z/right edited)]
                         (if right
                           (recur right)
                           (if edited
                             edited
                             dependencies-root))))]
     (some-> bumped-tree
             z/root
             ((fn [x] (spit project-file x)))))))
