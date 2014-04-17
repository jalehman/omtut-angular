(ns omtut-angular.core
    (:require-macros [cljs.core.async.macros :refer [go alt!]])
    (:require [goog.events :as events]
              [cljs.core.async :refer [put! <! >! chan timeout]]
              [om.core :as om :include-macros true]
              [sablono.core :as html :refer-macros [html]]
              [cljs-http.client :as http]
              [omtut-angular.utils :refer [guid]]))

(enable-console-print!)

;; We'll get to this later.
(def app-state
  (atom {:things []}))

;; Om apps are built in ClojureScript -- there are no templates. Thus, you
;; have the full power of the ClojureScript language at your disposal when
;; writing your UI.
(defn omtut-angular-app [app owner]
  (reify
    om/IRender
    (render [_]
      ;; Here we `let` a dummy string to be rendered into the template.
      (let [text (str "yet" "!")]
        (html
         [:div
          ;; And then concatenate it into our <p> element.
          [:p (str "Nothing here " text)]])))))

(om/root omtut-angular-app app-state
         {:target (.getElementById js/document "content")})
