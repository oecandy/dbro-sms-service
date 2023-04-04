(ns skec-sms-service.lora.message
	(:require [clojure.spec.alpha :as spec]
			  [clojure.data.json :as json]
			  [skec-sms-service.interface :refer [device-message
												  view]]
			  [clojure.set :as set]
			  [skec-sms-service.util.core :as util]
			  [taoensso.timbre :as timbre]))

(defn install-device-messages
	[]
	(defmethod device-message [:lora :QL20V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (select-keys [:battery
									   :waterQuality
									   :waterLevel]))]
			result))

	(defmethod device-message [:lora :FA20V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> (:sensor payload)
						 (select-keys [:temperature
									   :humidity
									   :state]))]
			result))

	(defmethod device-message [:lora :TH20V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (select-keys [:temperature
									   :humidity]))]
			result))

	(defmethod device-message [:lora :FA20V2]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (select-keys [:temperature
									   :humidity
									   :state]))]
			result))

	(defmethod device-message [:lora :WL20V3]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (select-keys [:battery
									   :waterLevel]))]
			result))

	(defmethod device-message [:lora :TP20V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (select-keys [:battery
									   :temperature]))]
			result))

	(defmethod device-message [:lora :SG20V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (update :co float)
						 (update :co / 100)
						 (update :o2 float)
						 (update :o2 / 100)
						 (update :h2s float)
						 (update :h2s / 100)
						 (update :ch4 float)
						 (select-keys [:co
									   :co2
									   :o2
									   :h2s
									   :ch4
									   :status]))]
			result))

	(defmethod device-message [:lora :SG20V2]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (update :co float)
						 (update :o2 float)
						 (update :h2s float)
						 (update :ch4 float)
						 (select-keys [:co
									   :co2
									   :o2
									   :h2s
									   :ch4]))]
			result))

	(defmethod device-message [:lora :SG20V3]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (update :co float)
						 (update :o2 float)
						 (update :h2s float)
						 (update :ch4 float)
						 (update :temperature float)
						 (update :humidity float)
						 (select-keys [:co
									   :o2
									   :h2s
									   :ch4
									   :temperature
									   :humidity]))]
			result))

	(defmethod device-message [:lora :SG20V4]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> (:data payload)
						 (update :co float)
						 (update :o2 float)
						 (update :h2s float)
						 (update :ch4 float)
						 (update :temperature float)
						 (update :humidity float)
						 (select-keys [:co
									   :co2
									   :o2
									   :h2s
									   :ch4
									   :temperature
									   :humidity
									   :fire]))]
			result))

	(defmethod device-message [:lora :FR20V2]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> (:flow payload)
						 (select-keys [:totalFlow]))]
			result))

	(defmethod device-message [:lora :WQ20V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (select-keys [:waterQuality
									   :waterTemp
									   :battery]))]
			result)))

(install-device-messages)