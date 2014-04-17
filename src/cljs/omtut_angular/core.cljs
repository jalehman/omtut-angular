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

;; Clear out our hard-coded phones list
(def app-state
  (atom
   {:phones []}))

;; This is a sort of router for events. As we progress, actions that require complex operations
;; on the cursor, trips to external resources, etc will use events that get routed to a series
;; of handlers -- which are just normal CLJS functions.
(defn handle-event
  [app owner [event data]]
  (case event
    :load-phones (get-phones app)
    nil))

;; The meat of the display phone list display logic has been broken into a sub-component.
;; One advantage of this is that it allows us to test this specific piece of the application
;; independently from the event loop. More on this below.
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
         [:ul
          (for [phone (->> (filter #(search query (om/value %) [:name :snippet]) phones)
                           (sort-by (keyword order-prop)))]
            [:li
             (:name phone)
             [:p (:snippet phone)]])]]]))))

;; As our application grows larger, the *root* component (the top-level one) will be responsible
;; for handling *global concerns*. These are things like listening for route changes and
;; managing the currently displayed application "view". The advantage to this will become more
;; apparent when the application grows larger.
(defn omtut-angular-app
  [{:keys [phones] :as app} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:chans {:events (chan) :kill (chan)}})
    om/IWillMount
    (will-mount [_]
      ;; IWillMount is only invoked once, making it an ideal place for a go loop. Events
      ;; are received here and passed to the router, along with the owning component and
      ;; cursor.
      (let [{:keys [events kill]} (om/get-state owner :chans)]
        (go (loop []
              (alt!
               events ([v c] (handle-event app owner v) (recur))
               ;; It is generally considered good practice to provide a kill channel that
               ;; will be triggered when the component unmounts. This helps minimize the
               ;; amount of garbage collection that the browser will be required to do.
               kill   ([v c] (prn "Closing event loop.")))))
        (put! events [:load-phones nil])))
    om/IWillUnmount
    (will-unmount [_]
      (put! (om/get-state owner [:chans :kill]) :kill))
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.container
        ;; We pass state along to the phones component in the event that it will
        ;; need to communicate with its parent over core.async channels.
        (om/build phones-list phones {:state state})]))))

(defn run! []
  (om/root omtut-angular-app app-state
           {:target (.getElementById js/document "content")}))

(set! (.-onload js/window) run!)
