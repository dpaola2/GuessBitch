(ns guessbitch.handlers
    (:use 
        guessbitch.database
        compojure.core
        compojure.handler
        [clojure.data.json :only (json-str write-json read-json)]
        ring.adapter.jetty
        [ring.middleware params reload stacktrace]
        hiccup.core)
    (:require
        [compojure.route :as route]))

(defn state-handler [{params :params}]
    (try
        (str (retrieve-state (params :id)))
        (catch Exception e (str e))))

(defn action-handler [{params :params}]
    "Not Implemented")

(defn new-game-handler [params]
    (str (new-key (params :remote-addr))))

