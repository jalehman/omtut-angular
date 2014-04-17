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
