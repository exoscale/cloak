(defproject exoscale/cloak "0.1.1"
  :description ""
  :license {:name "ISC"}
  :url "https://github.com/exoscale/cloak"
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :deploy-repositories [["snapshots" :clojars] ["releases" :clojars]]
  :profiles {:dev  {:dependencies []}
             :test  {:dependencies []}}
  :source-paths ["src/clj"]
  :pedantic? :warn
  :global-vars {*warn-on-reflection* true})
