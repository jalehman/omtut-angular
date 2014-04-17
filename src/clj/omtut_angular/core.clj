(ns omtut-angular.core
    (:require [compojure.handler :as handler]
              [compojure.route :as route]
              [compojure.core :refer [GET POST defroutes]]
              [ring.util.response :as resp]
              [cheshire.core :as json]
              [clojure.java.io :as io]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})

(defroutes app-routes

  (route/resources "/")

  (GET "*" [] (resp/file-response "resources/public/index.html"))

  (route/not-found "Page not found"))

(def app
  (-> #'app-routes
      (handler/api)))
