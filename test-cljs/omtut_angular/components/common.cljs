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

(defn wrap-component
  "Takes a component and options. Wraps a component with a call to `om/build`
   so that we can mount the component with a call to `om/root` in a DOM container
   (see `new-container!` above) and initialize it with the state/options that it
   might contain in the wild."
  [component & {:keys [init-state state opts]}]
  (fn [app owner]
    (om/component
     (om/build component app
               {:init-state init-state :state state :opts opts}))))

(defn sim-click!
  [node]
  (js/React.addons.TestUtils.Simulate.click node))
