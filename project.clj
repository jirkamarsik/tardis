(defproject tardis "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.cemerick/pomegranate "0.3.1"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/core.logic "0.8.10"]]
  :repl-options {:init (do (require 'tardis.core)
                           (tardis.core/wake-up-tardis))})
