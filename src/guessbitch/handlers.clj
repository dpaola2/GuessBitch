(ns guessbitch.handlers
    (:use 
        compojure.core
        compojure.handler
        [clojure.data.json :only (json-str write-json read-json)]
        ring.adapter.jetty
        [ring.middleware params reload stacktrace]
        hiccup.core)
    (:require
        [compojure.route :as route]
        redis
        digest))

(defn new-key [ip-addr] 
    (digest/md5 ip-addr))

(defn state-handler [{params :params}]
    (str "Querying for: " (params :id)))

(defn action-handler [{params :params}]
    (str params))

(defn new-game-handler [params]
    (str (new-key (params :remote-addr))))

(def not-found-handler
    "404 Not Found")
