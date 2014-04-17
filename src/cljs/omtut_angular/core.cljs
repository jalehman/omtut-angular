(ns omtut-angular.core
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [cljs.core.async :refer [put! <! >! chan timeout]]
              [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [cljs-http.client :as http]
              [omtut-angular.utils :refer [guid handle-change search]]))

(enable-console-print!)

;; Add the age attributes into our cursor
(def app-state
  (atom
   {:phones
    [{:name "Nexus S" :age 1
      :snippet "Fast just got faster with Nexus S."}
     {:name "Motorola XOOM with Wi-Fi" :age 2
      :snippet "The Next, Next Generation tablet."}
     {:name "MOTOROLA XOOM" :age 3
      :snippet "The Next, Next Generation tablet."}]}))

(defn omtut-angular-app
  [{:keys [phones]} owner]
  (reify
    om/IInitState
    (init-state [_]
      ;; Our `order-prop` can be represented in component state.
      {:query "" :order-prop "age"})
    om/IRenderState
    (render-state [_ {:keys [query order-prop]}]
      ;; Our `handle-change` function will pull a string value of `order-prop` from the
      ;; event, so we store it as a string and call `keyword` on it here. Sorting only requires
      ;; use of a built-in CLJS function!
      (let [phones' (->> (filter #(search query (om/value %) [:name :snippet]) phones)
                         (sort-by (keyword order-prop)))]
        (html
         [:div.container
          [:div.row
           [:div.col-lg-2
            "Search: "
            [:input
             {:type "text" :value query
              :on-change #(handle-change % owner [:query])}]

            "Sort by:"
            [:select {:on-click #(handle-change % owner [:order-prop])
                      :value order-prop}
             [:option {:value "name"} "Alphabetical"]
             [:option {:value "age"}  "Newest"]]]

           [:div.col-lg-10
            [:ul
             (for [phone phones']
               [:li
                (:name phone)
                [:p (:snippet phone)]])]]]])))))

(defn run! []
  (om/root omtut-angular-app app-state
         {:target (.getElementById js/document "content")}))

(set! (.-onload js/window) run!)
