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
      (let [text (str "yet" "!")]
        (html
         [:div
          [:p (str "Nothing here " text)]])))))

(om/root omtut-angular-app app-state
         {:target (.getElementById js/document "content")})
