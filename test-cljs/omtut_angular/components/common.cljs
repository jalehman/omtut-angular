(ns omtut-angular.test.components.common
  "The only place I could find anything on testing in Om apps is in Sean Grove's Omchaya
   app. Here's the relevant file:

   https://github.com/sgrove/omchaya/blob/master/test-cljs/omchaya/components/common.cljs"
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]
            [dommy.core :as dommy]
            [om.core :as om :include-macros true])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(defn container-div []
  (let [id (str "container-" (gensym))]
    [(node [:div {:id id}]) (str "#" id)]))

(defn insert-container! [container]
  (dommy/append! (sel1 js/document :body) container))

(defn new-container! []
  (let [[n s] (container-div)]
    (insert-container! n)
    (sel1 s)))
