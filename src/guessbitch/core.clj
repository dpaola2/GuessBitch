(ns guessbitch.core
    (:use 
        guessbitch.handlers
        compojure.core
        compojure.handler
        ring.adapter.jetty
        [ring.middleware params reload stacktrace]
        hiccup.core)
    (:require
        [compojure.route :as route]))

(defroutes main-routes
    (GET "/state/:id" [] state-handler)
    (GET "/action/:id" [] action-handler)
    (GET "/new" [] new-game-handler)
    (route/not-found not-found-handler))

(defn -main [& args] 
    (run-jetty (-> 
                    (api main-routes)
                    (wrap-reload '(guessbitch.core guessbitch.handlers))) 
                {:port 8080 :join? false}))
