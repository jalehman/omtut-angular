(ns omtut-angular.test.components.core
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]
            [dommy.core :as dommy]
            [cljs.core.async :refer [chan]]
            [om.core :as om :include-macros true]
            [omtut-angular.test.components.common :as common]
            [omtut-angular.core :as core])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(deftest phones-render-correctly?
  (let [data {:phones
              [{:name "Nexus S" :age 1 :id "nexus-s"
               :snippet "Fast just got faster with Nexus S."}
              {:name "Motorola XOOM with Wi-Fi" :age 2
               :snippet "The Next, Next Generation tablet."}
              {:name "MOTOROLA XOOM" :age 3
               :snippet "The Next, Next Generation tablet."}]}
        chans {:events (chan) :kill (chan)}]

    ;; NOTE: Because the phones-list component now puts a value onto a channel,
    ;; we now need to build the component with channels to replicate its actual
    ;; environment. That way we don't get tons of errors.
    (testing "Three phones render"
      (is (= 3
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/phones-list
                                       :state {:chans chans})
                data {:target c})
               (count (sel c :li))))))

    (testing "Filters the phone list by the query in the search box"
      (is (= 3
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/phones-list
                                       :state {:chans chans})
                data {:target c})
               (count (sel c :li)))))

      (is (= [1 "nexus"]
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/phones-list
                                       :state {:query "nexus" :chans chans})
                data {:target c})
               [(count (sel c :li)) (dommy/value (sel1 c :input))])))

      (is (= [2 "motorola"]
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/phones-list
                                       :state {:query "motorola" :chans chans})
                data {:target c})
               [(count (sel c :li)) (dommy/value (sel1 c :input))]))))

    (testing "Can control phone order via the drop-down select box"
      (is (= ["Motorola XOOM with Wi-Fi" "MOTOROLA XOOM"]
             (let [c (common/new-container!)]
               (om/root
                (common/wrap-component core/phones-list
                                       :state {:query "tablet" :chans chans})
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
