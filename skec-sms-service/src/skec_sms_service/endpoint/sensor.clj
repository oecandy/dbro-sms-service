(ns skec-sms-service.endpoint.sensor
	(:require [taoensso.timbre :as timbre]
			  [skec-sms-service.interface :refer [view
												  raw-query]]
			  [ring.util.response :as r]
			  [clojure.spec.alpha :as s]
			  [clojure.spec.gen.alpha :as g]
			  [clojure.spec.alpha :as spec]
			  [clojure.data.json :as json]
			  [failjure.core :as fail]
			  [cheshire.generate :as gen]
			  [skec-sms-service.util.core :as util]))

(s/def ::x-authorization string?)

(s/def ::sensor_idx number?)
(s/def ::hardware_type string?)
(s/def ::network_type string?)
(s/def ::model_id string?)
(s/def ::serial_no string?)

(s/def ::sensor_alarm_idx number?)
(s/def ::sensor_alarm_name string?)
(s/def ::sms_template_idx number?)
(s/def ::sensor_key string?)
(s/def ::higher_value number?)
(s/def ::lower_value number?)
(s/def ::contact_group_idx number?)
(s/def ::use_start_time string?)
(s/def ::use_end_time string?)
(s/def ::use_dow string?)

(defn handle-select-sensor-list
	"센서 리스트 조회"
	[{{{:keys [x-authorization]} :header} :parameters}]
	(let [result (view [:sensor :select :all] {})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-select-sensor
	"센서 리스트 조회"
	[{{{:keys [x-authorization]} :header
	   {:keys [sensor_idx]}      :path} :parameters}]
	(let [result (-> (view [:sensor :select :query] {:sensor_idx sensor_idx})
					 (first))]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-create-sensor
	"센서 리스트 조회"
	[{{{:keys [x-authorization]} :header
	   {:keys [hardware_type
			   network_type
			   model_id
			   serial_no]}       :body} :parameters}]
	(let [result (view [:sensor :insert] {:hardware_type hardware_type
										  :network_type  network_type
										  :model_id      model_id
										  :serial_no     serial_no})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-update-sensor
	"센서 리스트 조회"
	[{{{:keys [x-authorization]} :header
	   {:keys [sensor_idx]}      :path
	   {:keys [hardware_type
			   network_type
			   model_id
			   serial_no]}       :body} :parameters}]
	(let [result (view [:sensor :update] {:sensor_idx    sensor_idx
										  :hardware_type hardware_type
										  :network_type  network_type
										  :model_id      model_id
										  :serial_no     serial_no})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-select-sensor-alarm-list
	"센서 리스트 조회"
	[{{{:keys [x-authorization]} :header
	   {:keys [sensor_idx]}      :path} :parameters}]
	(let [result (view [:sensor-alarm :select :query] {:sensor_idx sensor_idx})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-select-sensor-alarm
	"센서 리스트 조회"
	[{{{:keys [x-authorization]}  :header
	   {:keys [sensor_idx
			   sensor_alarm_idx]} :path} :parameters}]
	(let [result (-> (view [:sensor-alarm :select :query] {:sensor_alarm_idx sensor_alarm_idx})
					 (first))]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-create-sensor-alarm
	"센서 리스트 조회"
	[{{{:keys [x-authorization]} :header
	   {:keys [sensor_idx]}      :path
	   {:keys [sms_template_idx
			   sensor_alarm_name
			   sensor_key
			   higher_value
			   lower_value
			   contact_group_idx
			   use_dow
			   use_end_time
			   use_start_time]}  :body} :parameters}]
	(let [result (view [:sensor-alarm :insert] {:sensor_idx        sensor_idx
												:sms_template_idx  sms_template_idx
												:sensor_alarm_name sensor_alarm_name
												:sensor_key        sensor_key
												:higher_value      higher_value
												:lower_value       lower_value
												:contact_group_idx contact_group_idx
												:use_dow           use_dow
												:use_end_time      use_end_time
												:use_start_time    use_start_time})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-update-sensor-alarm
	"센서 리스트 조회"
	[{{{:keys [x-authorization]}  :header
	   {:keys [sensor_idx
			   sensor_alarm_idx]} :path
	   {:keys [sms_template_idx
			   sensor_alarm_name
			   sensor_key
			   higher_value
			   lower_value
			   contact_group_idx
			   use_dow
			   use_end_time
			   use_start_time]}   :body} :parameters}]
	(let [result (view [:sensor-alarm :update] {:sensor_idx        sensor_idx
												:sensor_alarm_idx  :sensor_alarm_idx
												:sms_template_idx  sms_template_idx
												:sensor_alarm_name sensor_alarm_name
												:sensor_key        sensor_key
												:higher_value      higher_value
												:lower_value       lower_value
												:contact_group_idx contact_group_idx
												:use_dow           use_dow
												:use_end_time      use_end_time
												:use_start_time    use_start_time})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))


(def routes ["/sensor" {:swagger {:tags ["Sensor endpoint"]}}
			 [""
			  {:get  {:summary     "센서 리스트 조회"
					  :description (str "")
					  :parameters  {}
					  :handler     handle-select-sensor-list}
			   :post {:summary     "센서 생성"
					  :description (str "")
					  :parameters  {:body (s/keys :req-un [::hardware_type
														   ::network_type
														   ::model_id
														   ::serial_no])}
					  :handler     handle-create-sensor}}
			  ]
			 ["/:sensor_idx"
			  [""
			   {:get {:summary     "센서 상세 조회"
					  :description (str "")
					  :parameters  {:path (s/keys :req-un [::sensor_idx])}
					  :handler     handle-select-sensor}
				:put {:summary     "센서 정보 수정"
					  :description (str "")
					  :parameters  {:path (s/keys :req-un [::sensor_idx])
									:body (s/keys :req-un [::hardware_type
														   ::network_type
														   ::model_id
														   ::serial_no])}
					  :handler     handle-update-sensor}}]
			  ["/alarm"
			   [""
				{:get  {:summary     "센서 알람 리스트 조회"
						:description (str "")
						:parameters  {:path (s/keys :req-un [::sensor_idx])}
						:handler     handle-select-sensor-alarm-list}
				 :post {:summary     "센서 알람 생성"
						:description (str "")
						:parameters  {:path (s/keys :req-un [::sensor_idx])
									  :body (s/keys :req-un [::sms_template_idx
															 ::sensor_alarm_name
															 ::sensor_key
															 ::higher_value
															 ::lower_value
															 ::contact_group_idx
															 ::use_dow
															 ::use_end_time
															 ::use_start_time])}
						:handler     handle-create-sensor-alarm}}]
			   ["/:sensor_alarm_idx"
				{:get {:summary     "센서 알람 상세 조회"
					   :description (str "")
					   :parameters  {:path (s/keys :req-un [::sensor_idx
															::sensor_alarm_idx])}
					   :handler     handle-select-sensor-alarm}
				 :put {:summary     "센서 알람 정보 수정"
					   :description (str "")
					   :parameters  {:path (s/keys :req-un [::sensor_idx
															::sensor_alarm_idx])
									 :body (s/keys :req-un [::sms_template_idx
															::sensor_alarm_name
															::sensor_key
															::higher_value
															::lower_value
															::contact_group_idx
															::use_dow
															::use_end_time
															::use_start_time])}
					   :handler     handle-update-sensor-alarm}}]]]])
