(ns omtut-angular.core
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [goog.events :as events]
              [cljs.core.async :refer [put! <! >! chan timeout]]
              [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [cljs-http.client :as http]
              [omtut-angular.utils :refer [guid]]))

(enable-console-print!)

(def app-state
  (atom {:things []}))

(defn omtut-angular-app [app owner]
  (reify
    om/IRender
    (render [_]
      ;; Our HTML will always be written in CLJS. No templates!
      (html
       [:ul
        [:li
         [:span "Nexus S"]
         [:p "Fast just got faster with Nexus S"]]
        [:li
         [:span "Motorola XOOM with Wi-Fi"]
         [:p "The Next, Next Generation tablet."]]]))))

(om/root omtut-angular-app app-state
         {:target (.getElementById js/document "content")})
