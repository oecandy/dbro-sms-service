(defproject skec-sms-service "0.1.0-SNAPSHOT"
	:description "API server for SKP SMS Service"
	:url "https://smarf.kr/"
	:license {:name "Commercial license"
			  :url  "https://smarf.kr/"}
	:main skec-sms-service.core
	:dependencies [[org.clojure/tools.logging "1.0.0"]         ; EPL 1.0
				   [buddy "2.0.0"]                                      ; Apache License 2.0
				   [clj-time "0.15.2"]                                  ; EPL
				   [clojurewerkz/quartzite "2.1.0"]                     ; EPL
				   [clojurewerkz/machine_head "1.0.0"]                  ; EPL 2.0
				   [com.h2database/h2 "1.4.200"]                        ;
				   [com.draines/postal "2.0.3"]                         ; MIT
				   [clojure.jdbc/clojure.jdbc-c3p0 "0.3.3"]             ; Apache-2.0
				   [http-kit "2.3.0"]                                   ; Apache-2.0
				   [org.clojure/clojure "1.10.0"]                       ; Apache-2.0
				   [org.clojure/spec.alpha "0.2.176"]                   ; Apache-2.0
				   [org.clojure/data.json "0.2.7"]                      ; Apache-2.0
				   [org.clojure/data.csv "1.0.0"]                       ; Apache-2.0
				   [org.clojure/java.jdbc "0.7.11"]                     ; Apache-2.0
				   [ring "1.7.0"]
				   [ring/ring-defaults "0.3.2"]                         ; MIT License
				   [ring/ring-json "0.5.0"]                             ; MIT License
				   [ring/ring-anti-forgery "1.3.0"]                     ; MIT License
				   [ring/ring-jetty-adapter "1.8.0"]                    ; MIT License
				   [ring/ring-mock "0.4.0"]                             ; MIT License
				   [ring/ring-core "1.8.0"]                             ; MIT
				   [org.slf4j/slf4j-simple "2.0.0-alpha1"]              ; MIT
				   [prismatic/schema "1.1.12"]                          ; EPL
				   [com.taoensso/timbre "4.10.0"]
				   [metosin/reitit "0.5.5"]                             ; EPL
				   [metosin/kekkonen "0.5.2"]                           ; EPL
				   [metosin/muuntaja "0.6.6"]                           ; EPL
				   [metosin/ring-swagger "0.26.2"]                      ;EPL
				   [metosin/reitit-pedestal "0.5.2"]
				   [metosin/reitit-interceptors "0.5.2"]
				   [mysql/mysql-connector-java "8.0.21"]
				   [korma "0.4.3"]                                      ;EPL
				   [scenari "1.5.0"]
				   [failjure "2.0.0"]
				   [integrant "0.8.0"]
				   [clj-pdf "2.5.4"]
				   [yogthos/config "1.1.7"]
				   [ring-cors "0.1.13"]
				   [org.clojure/data.codec "0.1.1"]
				   [vlaaad/reveal "1.0.128"]                            ; for google firebase app
				   [com.mchange/c3p0 "0.9.5"]
				   [clj-serial "2.0.5"]]                                ; for google firebase app
	:plugins [[lein-ring "0.12.5"] [lein-codox "0.10.7"]]
	:ring {:handler skec-sms-service.endpoint.core/app
		   :init    skec-sms-service.core/-main
		   :join?   false
		   :async?  false
		   :port    9000}
	:profiles {:dev  {:jvm-opts       ["-Dlogfile.path=development"
									   "-Dorg.eclipse.jetty.LEVEL=DEBUG"
									   "-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]
					  :env            {:clj-env :dev}
					  :resource-paths ["config/dev"]
					  :main           skec-sms-service.core/-main}
			   :test {:jvm-opts       ["-Dlogfile.path=test"]
					  :env            {:clj-env :test}
					  :resource-paths ["config/test"]}
			   :prod {:jvm-opts       ["-Dlogfile.path=product"]
					  :env            {:clj-env :prod}
					  :resource-paths ["config/prod"]}}
	:repl-options {:init-ns skec-sms-service.core})
(comment "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010")
