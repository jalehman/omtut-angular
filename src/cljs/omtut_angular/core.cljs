(ns omtut-angular.core
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [goog.events :as events]
              [cljs.core.async :refer [put! <! >! chan timeout]]
              [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [cljs-http.client :as http]
              [omtut-angular.utils :refer [guid]]))

(enable-console-print!)

;; The app-state atom becomes our application's *cursor* when we mount the
;; application with the call to `om/root` below. This is where your application's
;; data should live if you want to render it into the DOM.
(def app-state
  (atom
   {:phones
    [{:name "Nexus S"
      :snippet "Fast just got faster with Nexus S."}
     {:name "Motorola XOOM with Wi-Fi"
      :snippet "The Next, Next Generation tablet."}
     {:name "MOTOROLA XOOM"
      :snippet "The Next, Next Generation tablet."}]}))

;; We'll destructure the cursor to get at the `phones` data
(defn omtut-angular-app
  [{:keys [phones]} owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:ul
        ;; There are no special "templating" constructs like `ng-repeat` needed for
        ;; rendering elements into the DOM -- we can just use plain CLJS functions
        ;; to manipulate our DOM elements like any other piece of data.
        (for [phone phones]
          [:li (:name phone)
           [:p (:snippet phone)]])]))))

(defn run! []
  (om/root omtut-angular-app app-state
         {:target (.getElementById js/document "content")}))

(set! (.-onload js/window) run!)
