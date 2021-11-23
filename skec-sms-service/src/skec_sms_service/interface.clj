(ns skec-sms-service.interface
	(:require [taoensso.timbre :as timbre]))

(def ^:dynamic *system* (ref nil))

(defmulti view
		  "Update device object view.
		  view implemented view package library.
		  e.g.) (view :member-join )"
		  (fn [keyword conn] keyword))

(defmethod view :default [keyword conn])

(defmulti raw-query
		  (fn [keyword query] keyword))

(defmethod raw-query :default [keyword query])

(def handle-delivery (fn []))
(def handle-on-connection-lost (fn []))

(defmulti send-app-message :type)

(defmethod send-app-message :default [_]
	(timbre/debug "Application message is not defined."))

(defmulti device-message
		  "Parsing device payload to system message
		  Or processing sensor rule
		  e.g.) (device-message [:mqtt :wifi :weather] )"
		  (fn [keyword conn] keyword))

(defmethod device-message :default [keyword conn])