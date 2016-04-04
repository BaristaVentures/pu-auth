(ns pu-auth.core
  (:require [monger.collection :as mc]
            [compojure.api.meta :refer [restructure-param]]
            [clojure.string :as str]))

(def db (atom nil))
(comment (reset! db (-> "mongodb://191.168.50.4/pickup_dev" monger.core/connect-via-uri :db)))

(defn from-bearer [token]
  (println token)
  (when token
    (let [auth-token (second (str/split token #"^Bearer "))]
      (mc/find-one-as-map @db
                          "authtokens"
                          {:token auth-token}))))

(defmethod restructure-param :auth
  [_ token {:keys [parameters lets body middlewares] :as acc}]
  "Make sure the request has Authorization header and that it's value is a valid token. Binds the value into a variable"
  (-> acc
     (update-in [:lets] into [{{token "authorization"} :headers} '+compojure-api-request+])
     (assoc-in [:swagger :parameters :header] {:authorization String})
     (assoc :body `((if-let [~'auth-token (from-bearer ~token)]
                      (do ~@body)
                      (ring.util.http-response/forbidden {:errors {:authorization "Wrong Session token."}}))))))
