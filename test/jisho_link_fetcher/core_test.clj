(ns jisho-link-fetcher.core-test
  (:require [clojure.test :refer :all]
            [jisho-link-fetcher.core :refer :all]))

(deftest jisho-test
  (let [link-set (extract-jisho-links "test/test-files/cute.org")]
    (testing "Parsed org-files have no duplicate links"
      (is (= (count link-set) (count (set link-set)) 2))))

  (let [definition-set (jisho-definitions-set "test/test-files/cute.org")]
    (testing "Seeing if these definitions contain anything"
      (is (not-any? nil? (map :definition definition-set))))

    (testing "Seeing if the definition set removes duplicates"
      (is (= (count definition-set) 1))))

  (let [cute (jisho-definition "かわいい")]
    (testing "Seeing if cute is cute"
      (is (clojure.string/includes? (:definition cute) "cute")))))
