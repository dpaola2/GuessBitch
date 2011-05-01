(ns guessbitch.database
    (:use 
        guessbitch.utils
        [clojure.data.json :only (json-str read-json)])
    (:require
        redis
        digest))

(defn new-key [ip-addr] 
    (digest/md5 ip-addr))

(defmacro redis-connect [form]
     `(redis/with-server ~{:host "127.0.0.1" :port 6379 :db 0} ~form))

(defn error-message [msg] 
    (json-str {:error msg}))

(defn retrieve-state [id]
    (redis-connect 
        (let [result (redis/get id)]
            (if (= nil result) 
                (error-message "No such state")
                (read-json result)))))

(defn set-state [id state]
    ; state should be a dictionary
    (let [result (redis-connect (redis/set id (json-str state)))]
        (if result 
            (retrieve-state id) 
            (error-message "Error setting game state"))))

