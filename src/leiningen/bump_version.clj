(ns leiningen.bump-version
  (:require [clojure.string :as s]
            [rewrite-clj.zip :as z]))

(defn- get-project-name-token [project-root-token]
  (some-> project-root-token
          (z/find-next-depth-first (fn [current-token]
                                     (= (some-> current-token
                                                z/prev
                                                z/sexpr)
                                        'defproject)))))

(defn- get-project-group-str [project-name-token]
  (some-> project-name-token
          z/sexpr
          str
          (s/split #"/")
          first))

(defn- get-project-old-version-str [project-name-token]
  (some-> project-name-token
          z/next
          z/sexpr
          str))

(defn- get-bumped-version-str [version-str project-old-version-str]
  (or version-str
      (let [splitted (s/split project-old-version-str #"\.")
            updated (update splitted 2 (fn [patch-version-str]
                                         (let [[_ current-version-str snapshot] (re-find #"(\d+)(-SNAPSHOT)?"
                                                                                         patch-version-str)]
                                           (if snapshot
                                             current-version-str
                                             (-> (Integer. current-version-str)
                                                 inc
                                                 str)))))]
        (s/join "." updated))))

(defn- get-base-token [project-name-token bumped-version-str]
  (or (some-> project-name-token
              z/next
              (z/edit (constantly bumped-version-str)))
      project-name-token))

(defn- get-dependencies-root-token [base-token]
  (some-> base-token
          (z/find-value :dependencies)
          z/right))

(defn- filter-dependency-group-bool [project-group-str project-old-version-str]
  (fn [current-token]
    (and (= (some-> current-token
                    z/sexpr
                    str
                    (s/split #"/")
                    first)
            project-group-str)
         (= (some-> current-token
                    z/next
                    z/sexpr
                    str)
            project-old-version-str))))

(defn- get-bumped-tree-token [dependencies-root-token project-group-str project-old-version-str bumped-version-str]
  (loop [root-token dependencies-root-token]
    (let [edited-token (some-> root-token
                               (z/find-next-depth-first (filter-dependency-group-bool project-group-str
                                                                                      project-old-version-str))
                               z/next
                               (z/edit (constantly bumped-version-str))
                               z/up)
          right-token (z/right edited-token)]
      (if right-token
        (recur right-token)
        (if edited-token
          edited-token
          dependencies-root-token)))))

(defn bump-version
  ([project]
   (bump-version project nil "project.clj"))
  ([project version-str]
   (bump-version project version-str "project.clj"))
  ([project version-str project-file]
   (let [project-root-token (z/of-file project-file)
         project-name-token (get-project-name-token project-root-token)
         project-group-str (get-project-group-str project-name-token)
         project-old-version-str (get-project-old-version-str project-name-token)
         bumped-version-str (get-bumped-version-str version-str project-old-version-str)
         base-token (get-base-token project-name-token bumped-version-str)
         dependencies-root-token (get-dependencies-root-token base-token)
         bumped-tree-token (get-bumped-tree-token dependencies-root-token
                                                  project-group-str
                                                  project-old-version-str
                                                  bumped-version-str)]
     (some-> bumped-tree-token
             z/root
             ((fn [x] (spit project-file x)))))))
