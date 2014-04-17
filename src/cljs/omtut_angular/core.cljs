(ns omtut-angular.core
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [cljs.core.async :as async :refer [put! <! >! chan]]
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

(.setEnabled history true)

(events/listen history EventType.NAVIGATE
               (fn [e]
                 (secretary/dispatch! (.-token e))))

(defn change-location!
  "Convenience function for programmatically changing route."
  [uri]
  (.setToken history uri))

;; Here's our phone list route
(defroute "/phones" []
  (put! nav-ch [:nav {:state :phones-list}]))

;; And the phone detail route
(defroute "/phones/:phone-id" {:as params}
  (put! nav-ch [:nav {:state :phone-view :route-params params}]))

;; The catch-all route. If we reach this, we put a non-matching state. See the root component for more.
(defroute "*" []
  (put! nav-ch [:nav {:state :no-op}]))

;; ============================================================================
;; Router & Handlers

;; Our get-phones function changes a bit because our cursor is changing in structure.
(defn get-phones
  "Fetch phones.json from the server and transact into the cursor."
  [app]
  (go (let [res (<! (http/get "phones/phones.json"))]
        (om/update! app [:phones-list :phones] (:body res)))))

(defn handle-event
  [app owner [event data]]
  (case event
    :load-phones (get-phones app)
    ;; Events of type "nav" are handled by transacting the route data into the cursor under the
    ;; :location key. We also put the :route-params into the cursor under the state's key. Note
    ;; the changes to the cursor below (reasons for doing things this way will become more
    ;; apparent later).
    :nav         (do
                   (om/update! app [:location] data)
                   (om/update! app [(:state data) :route-params] (:route-params data)))
    nil))

;; ============================================================================
;; Sub-components

;; Our stub phone-detail "view"
(defn phone-detail
  [{:keys [phones route-params]} owner]
  (om/component
   (html
    [:div "TBD: detail view for " [:span (:phone-id route-params)]])))

(defn phones-list
  [{:keys [phones]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:query "" :order-prop "age"})
    om/IWillMount
    (will-mount [_]
      ;; The phones-list component is now responsible for firing of the :load-phones event.
      (put! (om/get-state owner [:chans :events]) [:load-phones nil]))
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
  [{:keys [location] :as app} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:chans {:events (chan) :kill (chan)}})
    om/IWillMount
    (will-mount [_]
      (let [{:keys [events kill]} (om/get-state owner :chans)
            ;; We create a new channel that will pick up events both internal to the component,
            ;; as well as nav events.
            events' (async/merge [events nav-ch])]
        (go (loop []
              (alt!
               events' ([v c] (handle-event app owner v) (recur))
               kill    ([v c] (prn "Closing event loop.")))))))
    om/IWillUnmount
    (will-unmount [_]
      (put! (om/get-state owner [:chans :kill]) :kill))
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.container
        ;; And here's our equivalent of `ng-view`. When we match a state, render that
        ;; view into the app. Otherwise, change location to a base route.

        ;; Note: putting the route-params into the cursor under the state's key allows us
        ;; build the sub-components such that they are only aware of their own path into the
        ;; cursor. This is generally considered a best practice.
        (case (:state location)
          :phones-list (om/build phones-list (:phones-list app) {:state state})
          :phone-view  (om/build phone-detail (:phone-view app) {:state state})
          (change-location! "/phones"))]))))

;; ============================================================================
;; Om Initialization

;; We now have a key for each state, and a :phones entry under each. The reasoning behind this
;; will be more apparent in the next step.
(def app-state
  (atom
   {:phones-list {:phones [] :route-params nil}
    :phone-view  {:phones {} :route-params {}}
    :location {}}))

(defn run! []
  (om/root omtut-angular-app app-state
           {:target (.getElementById js/document "content")}))

(set! (.-onload js/window) run!)
