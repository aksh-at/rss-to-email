(ns datomic.ion.rustic.mailer-test
  (:require
   [clojure.test :as t]
   [datomic.client.api :as d]
   [datomic.ion.rustic.edn :as edn]
   [datomic.ion.rustic.mailer :as mailer]
   [datomic.ion.rustic.poller :as poller]
   [datomic.ion.rustic.test-fixtures :as tf]))

(tf/test-setup)

(def rss-post "test/datomic/ion/rustic/fixtures/rss-post.edn")

(def atom-post "test/datomic/ion/rustic/fixtures/atom-post.edn")

(def attr-link-post {:tag :entry,
                     :attrs nil,
                     :content
                     [{:tag :title, :attrs nil, :content ["stuff"]}
                      {:tag :link,
                       :attrs {:href "abc.xyz"},
                       :content nil}
                      {:tag :content,
                       :attrs {:type "html"},
                       :content
                       ["potato"]}]})

(def content-no-desc-post {:tag :entry,
                   :attrs nil,
                   :content
                   [{:tag :title, :attrs nil, :content ["stuff"]}
                    {:tag :link,
                     :attrs nil,
                     :content ["abc.xyz"]}
                    {:tag :content,
                     :attrs {:type "html"},
                     :content
                     ["potato"]}]})

(def regular-post {:tag :entry,
                   :attrs nil,
                   :content
                   [{:tag :title, :attrs nil, :content ["stuff"]}
                    {:tag :link,
                     :attrs nil,
                     :content ["abc.xyz"]}
                    {:tag :description,
                     :attrs {:type "html"},
                     :content
                     ["potato"]}]})

(t/deftest get-homepage-tests
  (t/testing "works w/ prefix"
    (t/is (= (mailer/get-homepage-from-feed "https://ab.xyz/index.xml") "ab.xyz"))
    (t/is (= (mailer/get-homepage-from-feed "http://ab.xyz/index.xml") "ab.xyz")))
  (t/testing "works without prefix"
    (t/is (= (mailer/get-homepage-from-feed "ab.xyz/index.xml") "ab.xyz"))))


(t/deftest test-format-subject-line
  (def new-posts (-> rss-post edn/read (poller/get-new-posts nil)))
  (t/is (= (mailer/format-subject-line "https://ab.xyz/index.xml" new-posts) "2 new updates from ab.xyz")))

(t/deftest test-get-post-link
  (t/is (= (mailer/get-post-link regular-post) "abc.xyz"))
  (t/is (= (mailer/get-post-link attr-link-post) "abc.xyz")))

(t/deftest test-get-post-description
  (t/is (= (mailer/get-post-description regular-post) "potato"))
  (t/is (= (mailer/get-post-description content-no-desc-post) "potato")))


(t/deftest test-format-body
  ;; mock get-description so that the output isn't stupid long
  (with-redefs [datomic.ion.rustic.mailer/get-post-description (fn [_] "potato")]
    (def new-posts (-> atom-post edn/read (poller/get-new-posts nil)))
    (t/is (= (mailer/format-body "url" new-posts) "<html><div><a href=https://danluu.com/corrections/><h1>Major errors on this blog (and their corrections)</h1></a><p>potato <a href=https://danluu.com/corrections/>...</a> </p></div>\n<div><a href=https://danluu.com/people-matter/><h1>Individuals matter</h1></a><p>potato <a href=https://danluu.com/people-matter/>...</a> </p></div>\n<div><a href=https://danluu.com/culture/><h1>Culture matters</h1></a><p>potato <a href=https://danluu.com/culture/>...</a> </p></div>\n<div><a href=https://danluu.com/look-stupid/><h1>Willingness to look stupid</h1></a><p>potato <a href=https://danluu.com/look-stupid/>...</a> </p></div>\n<div><a href=https://danluu.com/learn-what/><h1>What to learn</h1></a><p>potato <a href=https://danluu.com/learn-what/>...</a> </p></div>\n<div><a href=https://danluu.com/productivity-velocity/><h1>Some reasons to work on productivity and velocity</h1></a><p>potato <a href=https://danluu.com/productivity-velocity/>...</a> </p></div>\n<div><a href=https://danluu.com/in-house/><h1>The value of in-house expertise</h1></a><p>potato <a href=https://danluu.com/in-house/>...</a> </p></div>\n<div><a href=https://danluu.com/why-benchmark/><h1>Some reasons to measure</h1></a><p>potato <a href=https://danluu.com/why-benchmark/>...</a> </p></div>\n<div><a href=https://danluu.com/essential-complexity/><h1>Against essential and accidental complexity</h1></a><p>potato <a href=https://danluu.com/essential-complexity/>...</a> </p></div>\n<div><a href=https://danluu.com/car-safety/><h1>How do cars fare in crash tests they're not specifically optimized for?</h1></a><p>potato <a href=https://danluu.com/car-safety/>...</a> </p></div>\n<div><a href=https://danluu.com/voyager-story/><h1>Finding the Story</h1></a><p>potato <a href=https://danluu.com/voyager-story/>...</a> </p></div>\n<div><a href=https://danluu.com/tracing-analytics/><h1>A simple way to get more value from tracing</h1></a><p>potato <a href=https://danluu.com/tracing-analytics/>...</a> </p></div>\n<div><a href=https://danluu.com/metrics-analytics/><h1>A simple way to get more value from metrics</h1></a><p>potato <a href=https://danluu.com/metrics-analytics/>...</a> </p></div>\n<div><a href=https://danluu.com/corp-eng-blogs/><h1>How (some) good corporate engineering blogs are written</h1></a><p>potato <a href=https://danluu.com/corp-eng-blogs/>...</a> </p></div>\n<div><a href=https://danluu.com/cli-complexity/><h1>The growth of command line options, 1979-Present</h1></a><p>potato <a href=https://danluu.com/cli-complexity/>...</a> </p></div></html>"))))
