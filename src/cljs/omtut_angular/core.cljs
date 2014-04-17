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

(def nav-ch (chan))

(def history (Html5History.))

(.setEnabled history true)

(events/listen history EventType.NAVIGATE
               (fn [e]
                 (secretary/dispatch! (.-token e))))

(defn change-location!
  "Convenience function for programmatically changing route."
  [uri]
  (.setToken history uri))

(defroute "/phones" []
  (put! nav-ch [:nav {:state :phones-list}]))

(defroute "/phones/:phone-id" {:as params}
  (put! nav-ch [:nav {:state :phone-view :route-params params}]))

(defroute "*" []
  (put! nav-ch [:nav {:state :no-op}]))

;; ============================================================================
;; Router & Handlers

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
;; Sub-components

;; This helps a bit, but not much.
(defn spec-list
  [data owner {:keys [pairs title]}]
  (om/component
   (html
    [:li [:span title]
     [:dl
      (for [[name k] pairs]
        [:span
         [:dt name]
         [:dd (str (get data k))]])]])))

(defn phone-detail
  [{:keys [phones route-params]} owner]
  (reify
    om/IWillMount
    (will-mount [_]
      ;; On mount, trigger the :load-phone event. The cool thing about this is
      ;; that if the phone already exists in the cursor under the phone-id, we won't
      ;; hit the server for it.
      (put! (om/get-state owner [:chans :events])
            [:load-phone (:phone-id (om/value route-params))]))
    om/IRenderState
    (render-state [_ _]
      (let [{:keys [name images] :as phone} (get phones (:phone-id route-params))]
        ;; Yuck. Lots of boilerplate HTML stuff here. Nothing really interesting
        (html
         [:div
          [:img {:src (first images)}]
          [:h1 name]
          [:p (:description phone)]

          [:ul.phone-thumbs
           (for [img images]
             [:li [:img {:src img}]])]

          [:ul.specs
           [:li [:span "Availability and Networks"]
            [:dl
             [:dt "Availability"]
             (map (fn [a]
                    [:dd {:dangerouslySetInnerHTML {:__html a}}])
                  (:availability phone))]]

           (om/build spec-list (:battery phone)
                     {:opts {:title "Battery"
                             :pairs [["type" :type] ["Talk Time" :talkTime]
                                     ["Standby time (max)" :standbyTime]]}})

           (om/build spec-list (:storage phone)
                     {:opts {:title "Storage and Memory"
                             :pairs [["RAM" :ram] ["Internal Storage" :flash]]}})

           (om/build spec-list (:connectivity phone)
                     {:opts {:title "Connectivity"
                             :pairs [["Network Support" :cell] ["WiFi" :wifi] ["GPS" :gps]
                                     ["Bluetooth" :bluetooth] ["Infrared" :infrared]]}})

           (om/build spec-list (:android phone)
                     {:opts {:title "Android"
                             :pairs [["OS Version" :os] ["UI" :ui]]}})

           [:li [:span "Size and Weight"]
            [:dl
             [:dt "Dimensions"]
             (map (fn [dim] [:dd dim]) (get-in phone [:sizeAndWeight :dimensions]))
             [:dt "Weight"]
             [:dd (get-in phone [:sizeAndWeight :weight])]]]

           (om/build spec-list (:display phone)
                     {:opts {:title "Display"
                             :pairs [["Screen size" :screenSize]
                                     ["Screen resolution" :screenResolution]
                                     ["Touch screen" :touchScreen]]}})

           (om/build spec-list (:hardware phone)
                     {:opts {:title "Hardware"
                             :pairs [["CPU" :cpu] ["USB" :usb] ["FM Radio" :fmRadio]
                                     ["Audio / headphone jack" :audioJack]
                                     ["Accelerometer" :accelerometer]]}})

           [:li [:span "Camera"]
            [:dl
             [:dt "Primary"]
             [:dd (get-in phone [:camera :primary])]
             [:dt "Features"]
             [:dd (clojure.string/join ", " (get-in phone [:camera :features]))]]]

           [:li [:span "Additional Features"]
            [:dd (:additionalFeatures phone)]]]])))))

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
       [:div
        (case (:state location)
          :phones-list (om/build phones-list (:phones-list app) {:state state})
          :phone-view  (om/build phone-detail (:phone-view app) {:state state})
          ;; Secretary will dispatch to the correct route before this point is reached;
          ;; however, this will be triggered immediately afterwards due to the lack of
          ;; a matching initial state, thus redirecting us to "/phones". This ensures
          ;; that the desired page actually loads.
          (when-not (= (:state location) :init)
            (change-location! "/phones")))]))))

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
