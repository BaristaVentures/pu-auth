(ns pu-auth.core
  (:require [monger.collection :as mc]
            [compojure.api.meta :refer [restructure-param]]
            [clojure.string :as str])
  (:import org.bson.types.ObjectId
           java.util.Date))

(def db (atom nil))
(comment (reset! db (-> "mongodb://191.168.50.4/pickup_dev" monger.core/connect-via-uri :db)))

(defn from-bearer [token]
  (println token)
  (when token
    (let [auth-token (second (str/split token #"^Bearer "))]
      (when-let [authtoken-db (mc/find-one-as-map @db
                                                  "authtokens"
                                                  {:token auth-token})]
        (let [oid (ObjectId. (get-in authtoken-db
                                     [:context :user_id]))]
          (if (= (first (:scope authtoken-db)) "user")
            (mc/find-one-as-map @db
                                "users"
                                {:_id oid})
            (mc/find-one-as-map @db
                                "providercustomers"
                                {:_id oid})))))))

(defmethod restructure-param :auth
  [_ token {:keys [parameters lets body middlewares] :as acc}]
  "Make sure the request has Authorization header and that it's value is a valid token. Binds the value into a variable current-user"
  (-> acc
     (update-in [:lets] into [{{token "authorization"} :headers} '+compojure-api-request+])
     (assoc-in [:swagger :parameters :header] {:authorization String})
     (assoc :body `((if-let [~'current-user (from-bearer ~token)]
                      (do ~@body)
                      (ring.util.http-response/forbidden {:errors {:authorization "Wrong Session token."}}))))))
