(ns jisho-link-fetcher.jisho-parser
  (:use jsoup.soup)
  (:require [clojure.string :as str]))

(defn non-ruby-jisho-furigana
  "Given a Jisho document tree/subtree with no Ruby annotations, tries
  really hard to extract the furigiana version of the first word on
  the page"
  [doc]
  (let [split (str/split (.html (select ".text" doc)) #"\n")
        split (str/split (first split) #"<[^>]+>")
        re-han (re-seq #"\p{script=Han}" (first split))
        furi (select ".furigana span" doc)
        furi (map #(str "[" % "]") (text furi))
        is-split (= (count re-han) (count furi))]
    (->> (interleave (if is-split re-han split) (concat furi (repeat "")))
         (filter (complement #(= "[]" %)))
         (apply str))))

(defn jisho-furigana
  "Given a Jisho document tree/subtree, returns the Furigana'd version
  of the word, with the furigana in square brackets after sets of
  Kanji."
  [doc]
  (let [full (first (text (select ".text" doc)))]
    (if (or (not full) (empty? (re-seq #"\p{script=Han}" full)))
      full
      (if (empty? (select "div.japanese ruby *" doc))
        (non-ruby-jisho-furigana doc)
        (let [ruby (select "div.japanese ruby *" doc)
              furiganad (str (first (text ruby)) "[" (second (text ruby)) "]")]
          ((partial str/replace-first full) (first (text ruby)) furiganad))))))
