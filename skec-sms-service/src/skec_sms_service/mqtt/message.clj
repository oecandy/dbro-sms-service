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
	(defmethod device-message [:mqtt :3party :SVS10V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (set/rename-keys {:xFreq  :x-freq
										   :xSpeed :x-speed
										   :yFreq  :y-freq
										   :ySpeed :y-speed
										   :zFreq  :z-freq
										   :zSpeed :z-speed}))]
			result))

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

	(defmethod device-message [:mqtt :3party :SKW10V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  {:keys [windspeed
					  maxspeed
					  avgwind]} payload]
			{:windSpeed windspeed
			 :maxSpeed maxspeed
			 :avgWind avgwind}))

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
						 (update :wind-speed * 0.28)
						 (util/update-map util/fix-2-float-point))]
			result))

	(defmethod device-message [:mqtt :eth :skecw12b]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (-> payload
						 (set/rename-keys {:CO  :co
										   :CO2 :co2
										   :O2  :o2
										   :CH4 :ch4
										   :H2S :h2s})
						 (update :o2 / 100)
						 (update :co / 100)
						 (update :h2s / 100))]
			result))

	(defmethod device-message [:mqtt :out :WT30V1]
		[_ {:keys [payload
				   topic_info]}]
		(let [{:keys [serial_no]} topic_info
			  result (if (not (nil? (:wbgt payload)))
						 payload
						 nil)]
			result)))

(install-device-messages)
