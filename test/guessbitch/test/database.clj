(ns guessbitch.test.database 
    (:use
        guessbitch.database
        guessbitch.utils
        clojure.test))

(def *test-key* "test-foo")

(def *sample-state* 
    {
        :timestamp (now)
        :players ["bob" "alice"]
        :waiting "bob"
        :questions {
            :bob 5
            :alice 6
        }
        :boards {
            :bob {
                :kanye "exposed"
                :obama "flipped"
            }
            :alice {
                :bill-gates "exposed"
                :steve-jobs "flipped"
            }
        }
    })

(defn test-fixture [f] 
    (try
        (f) 
        (finally (redis-connect (redis/del *test-key*)))))


(deftest test-set-state
    (is (= *sample-state* (set-state *test-key* *sample-state*))))

(deftest test-redis
    (redis/with-server {:host "127.0.0.1" :port 6379 :db 0}
        (do
            (redis/set *test-key* "bar")
            (is (= "bar" (redis/get *test-key*))))))

(use-fixtures :each test-fixture)
