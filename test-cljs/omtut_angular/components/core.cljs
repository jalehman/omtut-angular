(ns omtut-angular.test.components.core
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]
            [dommy.core :as dommy]
            [om.core :as om :include-macros true]
            [omtut-angular.test.components.common :as common]
            [omtut-angular.core :as core])
  (:use-macros [dommy.macros :only [node sel sel1]]))

;; Since React constructs a "virtual" DOM, we need to verify that it renders correctly
;; into an "actual" DOM.
(deftest phones-render?
  ;; We begin by providing some sample data (in this case, the same data)
  (let [data {:phones
              [{:name "Nexus S"
                :snippet "Fast just got faster with Nexus S."}
               {:name "Motorola XOOM with Wi-Fi"
                :snippet "The Next, Next Generation tablet."}
               {:name "MOTOROLA XOOM"
                :snippet "The Next, Next Generation tablet."}]}]

    ;; We want to ensure that there are actually three phones in this case
    ;; that get rendered into the DOM
    (testing "Three phones render"
      (is (= 3
             ;; `new-container!` creates a DOM node with an auto-generated ID
             ;; and returns the ID, so that we can provide a :target for the call
             ;; to `om/root`
             (let [c (common/new-container!)]
               (om/root core/omtut-angular-app data {:target c})
               ;; We then grab the list items and count them. Simple!
               (count (sel c :li))))))))
