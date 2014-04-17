(ns omtut-angular.utils
  (:require [cljs.reader :as reader]
            [om.core :as om :include-macros true])
  (:import [goog.ui IdGenerator]))

(defn guid []
  (.getNextUniqueId (.getInstance IdGenerator)))

(defn handle-change [e owner ks]
  (let [text (.. e -target -value)]
;;     (prn "IN HANDLE_CHANGE:")
;;     (.log js/console (clj->js (.. e -target -value)))
;;     (prn "=========")
    (om/set-state! owner ks text)))

(defn- extract-query-text
  [data ks]
  (->> (select-keys data ks)
       (vals)
       (interpose " ")
       (apply str)
       (clojure.string/lower-case)))

(defn search
  "Given a query `s`, map `data`, and list of keys `ks`, extract
  all content at `ks` and search for `s`. Returns the match or nil."
  [s data ks]
  (let [query-text (extract-query-text data ks)
        s'         (clojure.string/lower-case s)]
    (re-find (re-pattern s') query-text)))
