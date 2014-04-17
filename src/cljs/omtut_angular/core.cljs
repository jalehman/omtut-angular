(ns omtut-angular.core
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [omtut-angular.views.phone-detail :refer [phone-detail]]
              [omtut-angular.views.phones-list :refer [phones-list]]
              [omtut-angular.router :as router]
              [cljs.core.async :as async :refer [put! <! >! chan]]
              [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [cljs-http.client :as http]))

(enable-console-print!)

(.setEnabled router/history true)

;; At this point the application has grown too large to be in a single file
;; for my tastes. I prefer having my routing stuff in a `router` namespace,
;; and sub-views of the root component each in their own namespace under `views`.
;; Of course, this is a matter of taste.

;; ============================================================================
;; Event Router & Handlers

(defn- get-phones-list*
  [app]
  (let [c (chan)]
    (go (if-not (empty? (get-in @app [:phones-list :phones]))
          (put! c (get-in @app [:phones-list :phones]))
          (let [{body :body} (<! (http/get "phones/phones.json"))]
            (put! c body))))
    c))

(defn get-phones-list
  "Fetch phones.json from the server and transact into the cursor. If the phones
   list already exists in the cursor, don't go out to the server."
  [app]
  (go (let [ps (<! (get-phones-list* app))]
        (om/update! app [:phones-list :phones] ps))))

(defn- get-phone-detail*
  [app phone-id]
  (let [c (chan)]
    (go (if-let [pd (get-in @app [:phone-view :phones phone-id])]
          (put! c pd)
          (let [{body :body} (<! (http/get (str "phones/" phone-id ".json")))]
            (put! c body))))
    c))

(defn get-phone-detail
  "Mirrors the list version, but with the phone detail."
  [app phone-id]
  (go (let [pd (<! (get-phone-detail* app phone-id))]
        (om/update! app [:phone-view :phones phone-id] pd))))

(defn handle-event
  [app owner [event data]]
  (case event
    :load-phones (get-phones-list app)
    :load-phone  (get-phone-detail app data)
    :nav         (do
                   (om/update! app [:location] data)
                   (om/update! app [(:state data) :route-params] (:route-params data)))
    nil))

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
            events' (async/merge [events router/nav-ch])]
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
       [:div
        (case (:state location)
          :phones-list (om/build phones-list (:phones-list app) {:state state})
          :phone-view  (om/build phone-detail (:phone-view app) {:state state})
          (when-not (= (:state location) :init)
            (router/change-location! "/phones")))]))))

;; ============================================================================
;; Om Initialization

(def app-state
  (atom
   {:phones-list {:phones [] :route-params nil}
    :phone-view  {:phones {} :route-params {}}
    :location    {:state :init}}))

(defn run! []
  (om/root omtut-angular-app app-state
           {:target (.getElementById js/document "content")}))

(set! (.-onload js/window) run!)
