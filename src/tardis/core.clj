(ns tardis.core
  (:require [cemerick.pomegranate :as pom]
            [clojure.java.io :as io]
            [clojure.tools.namespace.find :as ns-find]))

(defn wake-up-tardis []
  (let [source-file (:file (meta #'wake-up-tardis))
        source-dir (.substring source-file 0 (.lastIndexOf source-file "/"))]
    (doseq [ns (ns-find/find-namespaces-in-dir (io/file source-dir))]
      (require ns))))

(defn equip-sonic-screwdriver []
  (let [tardis-nss (filter #(.startsWith (name (ns-name %)) "tardis") (all-ns))
        tardis-publics (apply concat (map (comp vals ns-publics) tardis-nss))
        tardis-exports (filter (comp :sonic-screwdriver meta) tardis-publics)]
    (for [var tardis-exports]
      (symbol (name (ns-name (:ns (meta var)))) (name (:name (meta var)))))))

(defn install-library [coordinate]
  (pom/add-dependencies
   :coordinates [coordinate]
   :repositories (merge cemerick.pomegranate.aether/maven-central
                        {"clojars" "http://clojars.org/repo"})))
