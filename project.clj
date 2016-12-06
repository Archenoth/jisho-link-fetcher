(defproject jisho-link-fetcher "0.0.1"
  :description "An org-file [[jisho:<word>][Word]] extractor"
  :url "https://github.com/Archenoth/jisho-link-fetcher"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:uberjar {:aot :all}}
  :main jisho-link-fetcher.core
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-soup/clojure-soup "0.1.3"]
                 [org.clojure/data.csv "0.1.3"]])
