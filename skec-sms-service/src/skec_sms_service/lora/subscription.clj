(ns skec-sms-service.lora.subscription
	(:require [clojure.string :as str]
			  [clojurewerkz.machine-head.client :as mh]
			  [skec-sms-service.interface :refer [view
												  *system*
												  handle-on-connection-lost
												  device-message]]
			  [clj-time.core :as t]
			  [clj-time.coerce :as tc]
			  [taoensso.timbre :as timbre]
			  [skec-sms-service.util.core :as util]
			  [skec-sms-service.domain.sensor :as sensor]))

;;; Written by junyoung
(defn client [] (-> @*system* :lora :client))

(defn mqtt-action-wrap
	[func]
	(try
		(func)
		(catch Exception e (timbre/debug e))))

(defn process-payload
	"페이로드를 처리 프로세스"
	[topic-info payload device-info]
	(let [{:keys [sensor_idx]} device-info
		  {:keys [network_type]} topic-info
		  model_id (:deviceProfile payload)
		  device-map {:payload    payload
					  :topic-info topic-info
					  :sensor_idx sensor_idx}
		  payload-info (device-message [:lora (keyword model_id)] device-map)]
		(timbre/debug device-map)
		(timbre/debug payload-info)
		(timbre/debug [:lora (keyword model_id)])
		(if (not (nil? payload-info))
			(do
				(view [:sensor :update] {:sensor_idx sensor_idx
										 :payload (util/map-to-json payload-info)
										 :last_timestamp (tc/to-sql-time (t/now))})
				(sensor/process-sk-sensor device-map payload-info)))))

(defn get-topic-info
	"example topic 'application/+/device/+/event/up'"
	[topic]
	(let [[_ _ _ serial_no _ _] (str/split topic #"/")]
		{:topic        topic
		 :network_type "lora"
		 :serial_no    serial_no}))

(defn process-subscribe
	"토픽을 받아 처리할 데이터인지 판별"
	[^String topic ^bytes payload]
	(let [{:keys [serial_no] :as topic-info} (get-topic-info topic)
		  payload (-> (String. payload "UTF-8")
					  (util/json-to-map)
					  (:object))
		  device-info (-> (view [:sensor :select :query] {:serial_no serial_no})
						  first)]
		(cond
			(not (empty? device-info))
			(process-payload topic-info payload device-info))))

(defn handle-delivery
	[^String topic _ ^bytes payload]
	(process-subscribe topic payload))

(alter-var-root
	(var handle-on-connection-lost)
	(fn [f]
		(fn [^Throwable reason]
			(timbre/debug "Mqtt connection lost : "
						  (.getMessage reason)
						  (.printStackTrace reason)))))

(defn start-subscription
	"mqtt subscribe start"
	[client]
	(when-let [c client]
		(timbre/info "LoRa subscribe start!")
		(try
			(mh/subscribe c {"application/9/device/+/event/up" 2} handle-delivery)
			(mh/subscribe c {"application/8/device/+/event/up" 2} handle-delivery)
			(catch Exception e
				(mh/disconnect c)))))
