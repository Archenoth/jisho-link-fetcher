(ns jisho-link-fetcher.core
  (:gen-class)
  (:use jsoup.soup)
  (:require [clj-anki.core :as anki]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]))

(defn non-ruby-jisho-furigana
  "Given a full word and Jisho document with no Ruby annotations,
  tries really hard to extract the furigiana version of the first word
  on the page"
  [full doc]
  (let [split (str/split (.html (select "div.exact_block .text" doc)) #"\n")
        split (str/split (first split) #"<[^>]+>")
        re-han (re-seq #"\p{script=Han}" (first split))
        furi (select ".exact_block div.clearfix:first-of-type .furigana span" doc)
        furi (map #(str "[" % "]") (text furi))
        is-split (= (count re-han) (count furi))]
    (->> (interleave (if is-split re-han split) (concat furi (repeat "")))
         (filter (complement #(= "[]" %)))
         (apply str))))

(defn jisho-furigana
  "Given a Jisho document, returns the Furigana'd version of the word,
  with the furigana in square brackets after sets of Kanji."
  [doc]
  (let [full (first (text (select "div.exact_block .text" doc)))]
    (if (or (not full) (empty? (re-seq #"\p{script=Han}" full)))
      full
      (if (empty? (select "div.exact_block div.japanese ruby *" doc))
        (non-ruby-jisho-furigana full doc)
        (let [ruby (select "div.exact_block div.japanese ruby *" doc)
              furiganad (str (first (text ruby)) "[" (second (text ruby)) "]")]
          ((partial str/replace-first full) (first (text ruby)) furiganad))))))

(defn jisho-definition
  "Grabs a definition if available for a Japanese word lookup"
  [word]
  (Thread/sleep 1000) ;; Be courteous, limit at 1 per second
  (let [doc (get! (str "http://jisho.org/search?utf8=%E2%9C%93&keyword=" word))]
    (println (str "Fetching " word "..."))
    {:word (-> (select "div.exact_block .text" doc) text first)
     :furigana (jisho-furigana doc)
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
      (filter #(not (empty? (:word %))) acc)
      (recur (rest words) (conj acc (jisho-definition (first words)))))))

(defn write-jisho-definition-anki-package
  "Given an org file and an Anki package location, creates a new .apkg
  file that can be imported into Anki from the set of definitions from
  the org file as fetched from Jisho."
  [org-file out-file]
  (anki/map-seq-to-package!
   (map (fn [definition]
          {:question (:word definition)
           :answers [(str (:furigana definition) "<br />" (:definition definition))]
           :tags #{"jisho"}})
        (jisho-definitions-set org-file))
   out-file))

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
   (write-jisho-definition-anki-package org-file out-file))
  ([] (println "Usage: jisho-link-fetcher <Org File> <Output apkg>"))
  ([x] (-main))
  ([x y & rest] (-main)))
