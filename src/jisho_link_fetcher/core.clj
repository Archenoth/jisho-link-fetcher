(ns jisho-link-fetcher.core
  (:gen-class)
  (:use jsoup.soup)
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]))

(defn jisho-definition
  "Grabs a definition if available for a Japanese word lookup"
  [word]
  (Thread/sleep 1000) ;; Be courteous, limit at 1 per second
  (let [doc (get! (str "http://jisho.org/search?utf8=%E2%9C%93&keyword=" word))]
    {:word (-> (select ".concept_light-representation .text" doc) text first)
     :definition (-> (select ".meaning-meaning" doc) text first)}))

(defn extract-jisho-links
  "Extracts all of the jisho: links from an org-file into a set."
  [org-file]
  (with-open [rdr (io/reader org-file)]
    (loop [lines (line-seq rdr)
           acc #{}]
      (if (empty? lines)
        acc
        (let [searches (map second (re-seq #"\[jisho:([^\]]+)\]" (first lines)))]
          (recur (rest lines) (apply conj acc searches)))))))

(defn jisho-definitions-set
  "Creates a set of definition maps for an org file with jisho searches."
  [org-file]
  (loop [words (extract-jisho-links org-file)
         acc #{}]
    (if (empty? words)
      acc
      (recur (rest words) (conj acc (jisho-definition (first words)))))))

(defn write-jisho-definition-txt-file
  "Grabs all the jisho: links in the passed-in org file and writes out
  their base form and definitions in the specified outfile"
  [org-file out-file]
  (with-open [outfile (io/writer out-file)]
    (csv/write-csv
     outfile
     (map (fn [targ] [(:word targ) (:definition targ)])
          (jisho-definitions-set org-file))
     :separator \tab)))

(defn -main
  "The not-so-idiomatic entrypoint"
  ([org-file out-file]
   (println "Reading" org-file "and writing to" out-file)
   (write-jisho-definition-txt-file org-file out-file))
  ([] (println "Usage: jisho-link-fetcher <Org File> <Output File>"))
  ([x] (-main))
  ([x y & rest] (-main)))
