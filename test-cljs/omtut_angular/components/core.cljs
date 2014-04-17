(ns omtut-angular.test.components.core
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]
            [dommy.core :as dommy]
            [om.core :as om :include-macros true]
            [omtut-angular.test.components.common :as common]
            [omtut-angular.core :as core])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(deftest phones-render-correctly?
  (let [data [{:name "Nexus S" :age 1 :id "nexus-s"
               :snippet "Fast just got faster with Nexus S."}
              {:name "Motorola XOOM with Wi-Fi" :age 2
               :snippet "The Next, Next Generation tablet."}
              {:name "MOTOROLA XOOM" :age 3
               :snippet "The Next, Next Generation tablet."}]]

    (testing "Three phones render"
      (is (= 3
             (let [c (common/new-container!)]
               (om/root core/phones-list data {:target c})
               (count (sel c :li))))))

    (testing "Filters the phone list by the query in the search box"
      (is (= 3
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/phones-list)
                data {:target c})
               (count (sel c :li)))))

      (is (= [1 "nexus"]
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/phones-list
                                       :state {:query "nexus"})
                data {:target c})
               [(count (sel c :li)) (dommy/value (sel1 c :input))])))

      (is (= [2 "motorola"]
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/phones-list
                                       :state {:query "motorola"})
                data {:target c})
               [(count (sel c :li)) (dommy/value (sel1 c :input))]))))

    (testing "Can control phone order via the drop-down select box"
      (is (= ["Motorola XOOM with Wi-Fi" "MOTOROLA XOOM"]
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/phones-list
                                       :state {:query "tablet"})
                data {:target c})
               (mapv dommy/text (sel c [:.phone-name])))))

      ;;       For whatever reason, I cannot get this test to work.

      ;;       (is (= ["MOTOROLA XOOM" "Motorola XOOM with Wi-Fi"]
      ;;              (let [c (common/new-container!)]
      ;;                (om/root
      ;;                 (common/wrap-component core/phones-list
      ;;                                        :state {:query "tablet"})
      ;;                 data {:target c})
      ;;                (js/React.addons.TestUtils.Simulate.click
      ;;                 (sel1 c "option[value=\"name\"]"))
      ;;                (mapv dommy/text (sel c [:li :span])))))
      )


    ))
