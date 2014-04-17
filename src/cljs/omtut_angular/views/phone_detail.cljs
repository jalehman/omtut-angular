(ns omtut-angular.views.phone-detail
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [cljs.core.async :as async :refer [put! <! >! chan]]
              [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]))

(defn- spec-list
  [data owner {:keys [pairs title]}]
  (om/component
   (html
    [:li [:span title]
     [:dl
      (for [[name k] pairs]
        [:span
         [:dt name]
         [:dd (get data k)]])]])))

(defn phone-detail
  [{:keys [phones route-params]} owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (put! (om/get-state owner [:chans :events])
            [:load-phone (:phone-id (om/value route-params))]))
    om/IRenderState
    (render-state [_ _]
      (let [{:keys [name images] :as phone} (get phones (:phone-id route-params))]
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
