(ns omtut-angular.core
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [cljs.core.async :refer [put! <! >! chan timeout]]
              [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [cljs-http.client :as http]
              [omtut-angular.utils :refer [guid handle-change search]]
              [secretary.core :as secretary :include-macros true :refer [defroute]]
              [goog.events :as events])
  (:import [goog.history Html5History]
           [goog.history EventType]))

(enable-console-print!)

;; ============================================================================
;; Routing (URLs)

;; Om is not a framework like Angular, and does not come with any built-in routing
;; facilities; however, in traditional Clojure fashion, there's a library for that!
;; It's called `secretary` and it's now required in the `ns` declaration above.

;; This global (gasp!) channel will be responsible for queueing navigation events.
(def nav-ch (chan))

;; Next, we'll configure Google Closure HTML5 history
(def history (Html5History.))

(events/listen history EventType.NAVIGATE
               (fn [e]
                 (secretary/dispatch! (.-token e))))

(defn change-location!
  "Convenience function for programmatically changing route."
  [uri]
  (.setToken history uri))

;; ============================================================================
;; Router & Handlers

(defn get-phones
  "Fetch phones.json from the server and transact into the cursor."
  [app]
  (go (let [res (<! (http/get "phones/phones.json"))]
        (om/update! app [:phones] (:body res)))))

(defn handle-event
  [app owner [event data]]
  (case event
    :load-phones (get-phones app)
    nil))

;; ============================================================================
;; Sub-components

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
            [:li.thumbnail
             [:a.thumb {:href (str "#/phones/" (:id phone))}
              [:img {:src (:imageUrl phone)}]]
             [:a.phone-name {:href (str "#/phones/" (:id phone))} (:name phone)]
             [:p (:snippet phone)]])]]]))))

;; ============================================================================
;; Root Component

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

;; ============================================================================
;; Om Initialization

(def app-state
  (atom
   {:phones []}))

(defn run! []
  (om/root omtut-angular-app app-state
           {:target (.getElementById js/document "content")}))

(set! (.-onload js/window) run!)
