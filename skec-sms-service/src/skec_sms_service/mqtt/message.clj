(ns skec-sms-service.mqtt.message
	(:require [clojure.spec.alpha :as spec]
			  [clojure.data.json :as json]
			  [skec-sms-service.interface :refer [device-message
												  view]]
			  [clojure.set :as set]
			  [skec-sms-service.util.core :as util]
			  [taoensso.timbre :as timbre]))

(defn install-device-messages
	[]
	(defmethod device-message [:mqtt :3party :SDT10V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (set/rename-keys {:finedust      :fine-dust
										   :ultraFinedust :ultra-fine-dust}))]
			result))

	(defmethod device-message [:mqtt :3party :SSD10V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  {:keys [leq
					  l10
					  lmax]} payload]
			payload))

	(defmethod device-message [:mqtt :wifi :weather]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (set/rename-keys {:outTemp     :temperature
										   :outHumidity :humidity
										   :hourRain    :hour-rain
										   :dayRain     :day-rain
										   :windDir     :wind-direction
										   :windSpeed   :wind-speed
										   :UV          :uv})
						 (select-keys [:temperature
									   :humidity
									   :hour-rain
									   :day-rain
									   :wind-direction
									   :wind-speed
									   :radiation
									   :pressure
									   :uv])
						 (util/update-map read-string)
						 (util/update-map util/fix-2-float-point))]
			result))

	(defmethod device-message [:mqtt :eth :skecw12b]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  {:keys [co
					  o2
					  h2s
					  ch4
					  co2]} payload]
			payload)))

(install-device-messages)
