(ns skec-sms-service.mqtt.subscription
	(:require [clojure.string :as str]
			  [clojurewerkz.machine-head.client :as mh]
			  [skec-sms-service.interface :refer [view
												  *system*
												  handle-on-connection-lost
												  device-message]]
			  [taoensso.timbre :as timbre]
			  [skec-sms-service.util.core :as util]
			  [skec-sms-service.domain.sensor :as sensor]))

;;; Written by junyoung
(defn client [] (-> @*system* :mqtt :client))

(defn mqtt-action-wrap
	[func]
	(try
		(func)
		(catch Exception e (timbre/debug e))))

(defn topic-str
	[network_type hardware_type serial flag]
	(apply str (interpose "/" ["farota" network_type hardware_type serial flag])))

(defn process-payload
	"페이로드를 처리 프로세스"
	[topic-info payload device-info]
	(let [{:keys [sensor_idx]} device-info
		  {:keys [hardware_type
				  network_type]} topic-info
		  device-map {:payload    payload
					  :topic-info topic-info
					  :sensor_idx sensor_idx}
		  payload-info (device-message [:mqtt (keyword network_type) (keyword hardware_type)] device-map)]
		(timbre/debug device-map)
		(timbre/debug payload-info)
		(timbre/debug [:mqtt (keyword network_type) (keyword hardware_type)])
		(if (not (nil? payload-info))
			(case hardware_type
				("SDT10V1" "SSD10V1" "weather" "skecw12b")
				(sensor/process-sk-sensor device-map payload-info)
				:default
				nil))))

(defn get-topic-info
	"example topic 'farota/{network_type}/{hardware_type}/{serial_no}/data'"
	[topic]
	(let [topic-str (if (zero? (str/index-of topic "/"))
						(str/replace-first topic #"/" "")
						topic)
		  [_ network_type hardware_type serial_no flag] (str/split topic-str #"/")]
		{:topic         topic
		 :network_type  network_type
		 :hardware_type hardware_type
		 :serial_no     serial_no
		 :flag          flag}))

(defn process-subscribe
	"토픽을 받아 처리할 데이터인지 판별"
	[^String topic ^bytes payload]
	(let [{:keys [serial_no
				  flag
				  hardware_type] :as topic-info} (get-topic-info topic)
		  payload (-> (String. payload "UTF-8")
					  (util/json-to-map))
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
		(timbre/info "LoRa MQTT subscribe start!")
		(try
			(mh/subscribe c {"/farota/wifi/weather/+/loop" 2} handle-delivery)
			(mh/subscribe c {"farota/3party/+/+/data" 2} handle-delivery)
			(mh/subscribe c {"farota/eth/skecw12b/#" 2} handle-delivery)
			(catch Exception e
				(mh/disconnect c)))))
