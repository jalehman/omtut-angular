(ns omtut-angular.views.phones-list
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [omtut-angular.utils :refer [handle-change search]]
              [cljs.core.async :as async :refer [put! <! >! chan]]
              [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]))

(defn phones-list
  [{:keys [phones]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:query "" :order-prop "age"})
    om/IWillMount
    (will-mount [_]
      (put! (om/get-state owner [:chans :events]) [:load-phones nil]))
    om/IRenderState
    (render-state [_ {:keys [query order-prop]}]
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
                    :default-value order-prop}
           [:option {:value "name"} "Alphabetical"]
           [:option {:value "age"}  "Newest"]]]

         [:div.col-lg-10
          [:ul.phones
           (for [phone (->> (filter #(search query (om/value %) [:name :snippet]) phones)
                            (sort-by (keyword order-prop)))]
             [:li.thumbnail
              [:a.thumb {:href (str "#/phones/" (:id phone))}
               [:img {:src (:imageUrl phone)}]]
              [:a.phone-name {:href (str "#/phones/" (:id phone))} (:name phone)]
              [:p (:snippet phone)]])]]]]))))
