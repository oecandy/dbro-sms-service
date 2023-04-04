(ns skec-sms-service.lora.core
	(:require [clojure.string :as str]
			  [clojure.data.json :as json]
			  [clojurewerkz.machine-head.client :as mh]
			  [skec-sms-service.interface :refer [handle-delivery
												  view
												  *system*
												  handle-on-connection-lost]]
			  [taoensso.timbre :as timbre]
			  [skec-sms-service.mqtt.message :as msg]
			  [skec-sms-service.util.core :refer :all]
			  [integrant.core :as ig]))


(defn client [] (-> @*system* :mqtt :client))

(defn mqtt-action-wrap
	[func]
	(try
		(func)
		(catch Exception e (timbre/debug e))))

(alter-var-root
	(var handle-on-connection-lost)
	(fn [f]
		(fn [^Throwable reason]
			(timbre/debug "MQTT connection lost : "
						  (.getMessage reason)
						  (.printStackTrace reason)))))