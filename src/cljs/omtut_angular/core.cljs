(ns omtut-angular.core
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [goog.events :as events]
              [cljs.core.async :refer [put! <! >! chan timeout]]
              [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [cljs-http.client :as http]
              [omtut-angular.utils :refer [guid handle-change search]]))

(enable-console-print!)

(def app-state
  (atom
   {:phones
    [{:name "Nexus S"
      :snippet "Fast just got faster with Nexus S."}
     {:name "Motorola XOOM with Wi-Fi"
      :snippet "The Next, Next Generation tablet."}
     {:name "MOTOROLA XOOM"
      :snippet "The Next, Next Generation tablet."}]}))

(defn omtut-angular-app
  [{:keys [phones]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:query ""})
    om/IRenderState
    (render-state [_ {:keys [query]}]
      (let [phones' (map #(assoc % :hidden
                            (not (search query (om/value %) [:name :snippet]))) phones)]
        (html
         [:div.container
          [:div.row
           [:div.col-lg-2
            "Search: "
            [:input
             {:type "text" :value query
              :on-change #(handle-change % owner [:query])}]]

           [:div.col-lg-10
            [:ul
             (for [phone phones']
               (when-not (:hidden phone)
                 [:li (:name phone)
                  [:p (:snippet phone)]]))]]]])))))

(defn run! []
  (om/root omtut-angular-app app-state
         {:target (.getElementById js/document "content")}))

(set! (.-onload js/window) run!)
