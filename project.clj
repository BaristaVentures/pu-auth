(defproject pu-auth "0.1.1"
  :description "PU Basic token validation"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.novemberain/monger "3.0.0-rc2"]
                 [metosin/compojure-api "1.0.2"]]
  :min-lein-version "2.0.0"

  :profiles {:uberjar {:omit-source true             
                       :aot :all
                       :uberjar-name "pu-auth.jar"}})
