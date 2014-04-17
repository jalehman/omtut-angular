(ns omtut-angular.core
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [cljs.core.async :refer [put! <! >! chan timeout]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs-http.client :as http]
            [omtut-angular.utils :refer [guid]]))

(enable-console-print!)

;; A utility function to update component state as the value in an input changes.
(defn handle-change [e owner ks]
  (let [text (.. e -target -value)]
    (om/set-state! owner ks text)))

(def app-state
  (atom
   {:phones
    [{:name "Nexus S"
      :snippet "Fast just got faster with Nexus S."}
     {:name "Motorola XOOM with Wi-Fi"
      :snippet "The Next, Next Generation tablet."}
     {:name "MOTOROLA XOOM"
      :snippet "The Next, Next Generation tablet."}]}))

;; We have to do a bit more work in this step than in the corresponding Angular step.
;; Angular is a framework and comes with quite a few tools -- in this case, filters.
;; The Angular tutorial uses a filter to do a client-side "search" on rendered data.
;; We'll roll our own variant.

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

(defn omtut-angular-app
  [{:keys [phones]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:query ""})
    om/IRenderState
    (render-state [_ {:keys [query]}]
      ;; Here we preprocess the phones, calling our `search` function on each one.
      ;; Each call to `om.core/set-state!` as made in our `handle-change` function
      ;; triggers a re-render, resulting in a newly "searched" list of phones. Those
      ;; that do not match are given a :hidden flag, and are not rendered.
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
