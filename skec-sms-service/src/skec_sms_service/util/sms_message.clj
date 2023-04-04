(ns skec-sms-service.util.sms-message
	(:require [clojure.spec.alpha :as spec]
			  [clojure.data.json :as json]
			  [skec-sms-service.interface :refer [sms-message
												  view]]
			  [clojure.string :as s]
			  [clojure.set :as set]
			  [skec-sms-service.util.core :as util]
			  [taoensso.timbre :as timbre]))

(defn install-sms-messages
	[]
	(defmethod sms-message [:scheduler :QL20V1]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [battery
					  waterQuality
					  waterLevel]} (-> sensor-data
									   (:payload)
									   (util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%waterQuality%" (str waterQuality))
				(s/replace #"%battery%" (str battery))
				(s/replace #"%waterLevel%" (str waterLevel)))))

	(defmethod sms-message [:scheduler :FA20V1]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [temperature
					  humidity
					  state]} (-> sensor-data
								  (:payload)
								  (util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%temperature%" (str temperature))
				(s/replace #"%humidity%" (str humidity))
				(s/replace #"%state%" (str state)))))

	(defmethod sms-message [:scheduler :FA20V2]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [temperature
					  humidity
					  state]} (-> sensor-data
								  (:payload)
								  (util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%temperature%" (str temperature))
				(s/replace #"%humidity%" (str humidity))
				(s/replace #"%state%" (str state)))))

	(defmethod sms-message [:scheduler :WL20V3]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [battery
					  waterQuality
					  waterLevel]} sensor-data
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%battery%" (str battery))
				(s/replace #"%waterQuality%" (str waterQuality))
				(s/replace #"%waterLevel%" (str waterLevel)))))

	(defmethod sms-message [:scheduler :TP20V1]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [battery
					  temperature]} (-> sensor-data
										(:payload)
										(util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" (str hyper_link_url))
				(s/replace #"%battery%" (str battery))
				(s/replace #"%temperature%" (str temperature)))))

	(defmethod sms-message [:scheduler :SG20V1]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [co
					  co2
					  o2
					  h2s
					  ch4]} (-> sensor-data
								(:payload)
								(util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%co%" (str co))
				(s/replace #"%co2%" (str co2))
				(s/replace #"%o2%" (str o2))
				(s/replace #"%h2s%" (str (format "%.4f" h2s)))
				(s/replace #"%ch4%" (str ch4)))))

	(defmethod sms-message [:scheduler :FR20V2]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [totalFlow]} (-> sensor-data
									  (:payload)
									  (util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%totalFlow%" (str totalFlow)))))

	(defmethod sms-message [:scheduler :WQ20V1]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [waterQuality
					  waterTemp
					  battery]} (-> sensor-data
									(:payload)
									(util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%waterQuality%" (str waterQuality))
				(s/replace #"%waterTemp%" (str waterTemp))
				(s/replace #"%battery%" (str battery)))))

	(defmethod sms-message [:scheduler :WS10V1]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [temperature
					  humidity
					  hour-rain
					  day-rain
					  wind-direction
					  wind-speed
					  radiation
					  pressure
					  uv]} (-> sensor-data
							   (:payload)
							   (util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%temperature%" (str temperature))
				(s/replace #"%humidity%" (str humidity))
				(s/replace #"%hour-rain%" (str hour-rain))
				(s/replace #"%day-rain%" (str day-rain))
				(s/replace #"%wind-direction%" (str wind-direction))
				(s/replace #"%wind-speed%" (str wind-speed))
				(s/replace #"%radiation%" (str radiation))
				(s/replace #"%pressure%" (str pressure))
				(s/replace #"%uv%" (str uv)))))

	(defmethod sms-message [:scheduler :AQ00V2]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]
			:as test}]
		(let [{:keys [co
					  co2
					  o2
					  h2s
					  ch4]} (-> sensor-data
								(:payload)
								(util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(timbre/debug test)
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%co%" (str co))
				(s/replace #"%co2%" (str co2))
				(s/replace #"%o2%" (str o2))
				(s/replace #"%h2s%" (str h2s))
				(s/replace #"%ch4%" (str ch4)))))

	(defmethod sms-message [:scheduler :SVS10V1]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [x-freq
					  x-speed
					  y-freq
					  y-speed
					  z-freq
					  z-speed]} (-> sensor-data
									(:payload)
									(util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%x-freq%" (str x-freq))
				(s/replace #"%x-speed%" (str x-speed))
				(s/replace #"%y-freq%" (str y-freq))
				(s/replace #"%y-speed%" (str y-speed))
				(s/replace #"%z-freq%" (str z-freq))
				(s/replace #"%z-speed%" (str z-speed)))))

	(defmethod sms-message [:scheduler :SDT10V1]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [fine-dust
					  ultra-fine-dust
					  temperature
					  humidity]} (-> sensor-data
									 (:payload)
									 (util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%fine-dust%" (str fine-dust))
				(s/replace #"%ultra-fine-dust%" (str ultra-fine-dust))
				(s/replace #"%temperature%" (str temperature))
				(s/replace #"%humidity%" (str humidity)))))

	(defmethod sms-message [:scheduler :SSD10V1]
		[_ {:keys [sensor-data
				   msg-message
				   sms-template
				   contact-group]}]
		(let [{:keys [leq
					  l10
					  lmax]} (-> sensor-data
								 (:payload)
								 (util/json-to-map))
			  {:keys [msg_form]} msg-message
			  {:keys [sensor_type_name
					  hyper_link_url]} sms-template
			  {:keys [group_name]} contact-group]
			(-> msg_form
				(s/replace #"%group_name%" group_name)
				(s/replace #"%sensor_type_name%" sensor_type_name)
				(s/replace #"%hyper_link_url%" hyper_link_url)
				(s/replace #"%leq%" (str leq))
				(s/replace #"%l10%" (str l10))
				(s/replace #"%lmax%" (str lmax))))))

(install-sms-messages)