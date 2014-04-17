(ns omtut-angular.core
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [cljs.core.async :refer [put! <! >! chan timeout]]
              [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [cljs-http.client :as http]
              [omtut-angular.utils :refer [guid handle-change search]]))

(enable-console-print!)

(defn get-phones
  "Fetch phones.json from the server and transact into the cursor."
  [app]
  (go (let [res (<! (http/get "phones/phones.json"))]
        (om/update! app [:phones] (:body res)))))

(def app-state
  (atom
   {:phones []}))

(defn handle-event
  [app owner [event data]]
  (case event
    :load-phones (get-phones app)
    nil))

(defn phones-list
  [phones owner]
  (reify
    om/IInitState
    (init-state [_]
      {:query "" :order-prop "age"})
    om/IRenderState
    (render-state [_ {:keys [query order-prop]}]
      (html
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
            ;; Very little to do here. Just add a few classes and some data here and there.
            [:li.thumbnail
             [:a.thumb {:href (str "#/phones/" (:id phone))}
              [:img {:src (:imageUrl phone)}]]
             [:a.phone-name {:href (str "#/phones/" (:id phone))} (:name phone)]
             [:p (:snippet phone)]])]]]))))

(defn omtut-angular-app
  [{:keys [phones] :as app} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:chans {:events (chan) :kill (chan)}})
    om/IWillMount
    (will-mount [_]
      (let [{:keys [events kill]} (om/get-state owner :chans)]
        (go (loop []
              (alt!
               events ([v c] (handle-event app owner v) (recur))
               kill   ([v c] (prn "Closing event loop.")))))
        (put! events [:load-phones nil])))
    om/IWillUnmount
    (will-unmount [_]
      (put! (om/get-state owner [:chans :kill]) :kill))
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.container
        (om/build phones-list phones {:state state})]))))

(defn run! []
  (om/root omtut-angular-app app-state
           {:target (.getElementById js/document "content")}))

(set! (.-onload js/window) run!)
