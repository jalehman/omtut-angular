(ns omtut-angular.test.components.core
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]
            [dommy.core :as dommy]
            [om.core :as om :include-macros true]
            [omtut-angular.test.components.common :as common]
            [omtut-angular.core :as core])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(deftest phones-render?
  (let [data {:phones
              [{:name "Nexus S"
                :snippet "Fast just got faster with Nexus S."}
               {:name "Motorola XOOM with Wi-Fi"
                :snippet "The Next, Next Generation tablet."}
               {:name "MOTOROLA XOOM"
                :snippet "The Next, Next Generation tablet."}]}]

    (testing "Three phones render"
      (is (= 3
             (let [c (common/new-container!)]
               (om/root core/omtut-angular-app data {:target c})
               (count (sel c :li))))))

    ;; Our "filter" uses a piece of component state called "query" to perform
    ;; its search. Because Om components are pure functions, we can simulate any
    ;; "query" by building it with its component state set up the way we want it.

    ;; We have a new testing utility function called `wrap-component` now. In
    ;; order to construct a component with state, we'll need a call to `om.core/build`;
    ;; however, we need to be able to mount the component into the dummy DOM for testing
    ;; with `om.core/root`. `wrap-component` allows us to do this. It's pretty simple.
    ;; Pure functions for the win!

    ;; Note: We could avoid this issue by putting the "query" into the cursor
    ;; instead of in component state -- that decision is entirely up to you as the
    ;; programmer. Either way, we should not have to write our application to be
    ;; compatible with our testing system -- what we want is the reverse.

    (testing "Filters the phone list by the query in the search box"
      (is (= 3
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/omtut-angular-app)
                data {:target c})
               (count (sel c :li)))))

      (is (= [1 "nexus"]
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/omtut-angular-app
                                       :state {:query "nexus"})
                data {:target c})
               [(count (sel c :li)) (dommy/value (sel1 c :input))])))

      (is (= [2 "motorola"]
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/omtut-angular-app
                                       :state {:query "motorola"})
                data {:target c})
               [(count (sel c :li)) (dommy/value (sel1 c :input))]))))))
