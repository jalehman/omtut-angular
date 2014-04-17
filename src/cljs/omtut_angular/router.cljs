(ns omtut-angular.router
    (:require [cljs.core.async :as async :refer [put! chan]]
              [secretary.core :as secretary :include-macros true :refer [defroute]]
              [goog.events :as events])
  (:import [goog.history Html5History]
           [goog.history EventType]))

;; ============================================================================
;; Routing (URLs)

(def nav-ch (chan))

(def history (Html5History.))

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
