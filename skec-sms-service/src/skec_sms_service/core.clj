(ns ^{:doc "The project for helping to sensor alarm sms service"}
	skec-sms-service.core
	(:require [org.httpkit.server :as server]
			  [clojurewerkz.machine-head.client :as mh]
			  [ring.middleware.defaults :refer :all]
			  [ring.adapter.jetty :as jetty]
			  [skec-sms-service.endpoint.core :as endpoint]
			  [skec-sms-service.interface :refer [*system*
												  handle-on-connection-lost]]
			  [skec-sms-service.mqtt.subscription :as mqtt]
			  [taoensso.timbre :as timbre]
			  [integrant.core :as ig]
			  [korma.db :as d]
			  [ring.middleware.reload :refer [wrap-reload]]
			  [config.core :refer [env]]
			  [clj-time.core :as t])
	(:gen-class))

(System/setProperty "org.eclipse.jetty.util.log.announce" "true")

(def config
	(ig/read-string
		(slurp
			(str "config/" (:profile-name env) "/config.edn"))))

(defmethod ig/init-key :mysql [_ opt]
	(do (let [db (d/create-db (d/mysql opt))
			  conn (d/default-connection db)]
			(require 'skec-sms-service.view.core)
			(ig/load-namespaces {:skec-sms-service.view.core {}})
			(into {:conn conn
				   :db   db} opt))))

(defmethod ig/halt-key! :mysql [_ _]
	(d/default-connection nil))

(defmethod ig/init-key :jetty [_ opts]
	(let [profile-name (:profile-name env)
		  app (if (#{"test" "dev"} profile-name)
				  (wrap-reload #'endpoint/app)
				  #'endpoint/app)]
		{:server (jetty/run-jetty app opts)}))

(defmethod ig/halt-key! :jetty [_ {:keys [server]}]
	(.stop server))

(defmethod ig/init-key :profile-name [_ keys]
	keys)

(defmethod ig/init-key :mqtt [_ {:keys [url login-info] :as opt}]
	(do
		(let [login-info (merge login-info
								{:opts               {:auto-reconnect true}
								 :on-connection-lost handle-on-connection-lost})
			  result {:client (mh/connect url login-info)}]
			(require 'skec-sms-service.mqtt.message)
			(ig/load-namespaces {:skec-sms-service.mqtt.message {}})
			(mqtt/start-subscription (:client result))
			(into opt result))))

(defmethod ig/halt-key! :mqtt [_ {:keys [client] :as system}]
	(mh/disconnect-and-close client))


(defmethod ig/init-key :lora [_ {:keys [url login-info] :as opt}]
	;(do
	;	(let [login-info (merge login-info
	;							{:opts               {:auto-reconnect true}
	;							 :on-connection-lost handle-on-connection-lost})
	;		  result {:client (mh/connect url login-info)}]
	;		(require 'skec-sms-service.mqtt.message)
	;		(ig/load-namespaces {:skec-sms-service.mqtt.message {}})
	;		(mqtt/start-subscription (:client result))
	;		(into opt result)))
	)

(defmethod ig/halt-key! :lora [_ {:keys [client] :as system}]
	;(mh/disconnect-and-close client)
	)

(defmethod ig/init-key :log [_ {:keys [level]}]
	(timbre/set-level! level))

(defn start!
	([]
	 (dosync
		 (ref-set *system* (ig/init config))))
	([keys]
	 (dosync
		 (ref-set *system* (ig/init config keys)))))

(defn stop!
	([]
	 (when (map? @*system*)
		 (dosync
			 (ref-set *system* (ig/halt! @*system*)))))
	([keys]
	 (when (and (map? @*system*) (seq keys))
		 (dosync
			 (ref-set *system* (ig/halt! @*system* keys))))))

(defn -main
	"Start application"
	[& args]
	(timbre/info "Start SKP SMS Service Application.")
	(start!))
