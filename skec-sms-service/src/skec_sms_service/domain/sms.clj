(ns skec-sms-service.domain.sms
	(:require [skec-sms-service.interface :refer [device-message
												  view
												  raw-query
												  sms-message]]
			  [skec-sms-service.util.core :as util]
			  [clj-time.core :as t]
			  [clj-time.coerce :as tc]
			  [clojure.string :as s]
			  [taoensso.timbre :as timbre]))

;INSERT INTO EM_TRAN
;(TRAN_PHONE, TRAN_CALLBACK, TRAN_STATUS, TRAN_DATE, TRAN_MSG, TRAN_TYPE)
;VALUES ('01066984294', '01066984294', '1', NOW(), 'Test Message 입니다', 4);

(defn send_mms_db_query
	"execute sql query in dbro database to send mms."
	[message {:keys [phone_number]}]
	(let [mms-id (-> (view [:mms :insert] {:FILE_CNT    1
										   :MMS_BODY    message
										   :MMS_SUBJECT ""})
					 (:generated_key))]
		(view [:sms :insert] {:TRAN_PHONE    phone_number
							  :TRAN_CALLBACK "0261192221"
							  :TRAN_STATUS   "1"
							  :TRAN_DATE     (tc/to-sql-time (t/now))
							  :TRAN_TYPE     6
							  :TRAN_ETC4     mms-id})))

(defn send_sms_db_query
	"execute sql query in dbro database to send sms."
	[default-message {:keys [name
							 phone_number
							 position
							 department]
					  :as   contact}]
	(let [message (-> default-message
					  (s/replace #"%position%" position)
					  (s/replace #"%department%" department)
					  (s/replace #"%name%" name))]
		(timbre/debug message)
		(if (> 90 (util/get-string-byte-length message))
			;(timbre/debug "This message's enough to be sent in SMS format.") ; 90byte 미만 일때, SMS 문자 전송
			;(timbre/debug "This message's over to 90 bytes so sent it in MMS format.")
			(view [:sms :insert] {:TRAN_PHONE    phone_number
								  :TRAN_CALLBACK "0261192221"
								  :TRAN_STATUS   "1"
								  :TRAN_DATE     (tc/to-sql-time (t/now))
								  :TRAN_TYPE     4
								  :TRAN_MSG      message})
			(send_mms_db_query message contact))))                   ; 90byte 초과 일때, MMS 문자 전송

(defn send-alarm-message
	"generating a message for sending alarm sms."
	[sensor-alarm sensor-value status]
	;(view [:sms :insert] message)
	(let [{:keys [sensor_alarm_idx
				  contact_group_idx]} sensor-alarm
		  contact-list (view [:contact :select :query]
							 {:contact_group_idx contact_group_idx})
		  {:keys [unit
				  group_name
				  sensor_type_name
				  hyper_link_url
				  over_state
				  max_state
				  level_3_over_state
				  under_state
				  min_state
				  level_3_under_state
				  msg_form]} (-> (raw-query [:select :sms-message :sensor-alarm-idx]
											{:sensor_alarm_idx sensor_alarm_idx})
								 (first))
		  contact-count (count contact-list)
		  sensor-state (case status
						   "03" level_3_under_state
						   "02" min_state
						   "01" under_state
						   "21" over_state
						   "22" max_state
						   "23" level_3_over_state)
		  default-message (-> msg_form
							  (s/replace #"%group_name%" group_name)
							  (s/replace #"%unit%" unit)
							  (s/replace #"%sensor_value%" (str sensor-value))
							  (s/replace #"%sensor_type_name%" (str sensor_type_name))
							  (s/replace #"%hyper_link_url%" (str hyper_link_url))
							  (s/replace #"%sensor_state%" sensor-state))]
		(if (> contact-count 0)
			(loop [x 0]
				(if (< x contact-count)
					(do
						(send_sms_db_query default-message (nth contact-list x))
						(recur (inc x))))))))

(defn send-schedule-message
	"sending scheduler message"
	[{:keys [sensor_idx
			 sms_template_idx
			 contact_group_idx]
	  :as   scheduler}]
	(let [{:keys [model_id]
		   :as   sensor-data} (-> (view [:sensor :select :query] {:sensor_idx sensor_idx})
								  (first))
		  {:keys [msg_template_idx]
		   :as   sms-template} (-> (view [:sms-template :select :query] {:sms_template_idx sms_template_idx})
								   (first))
		  msg-message (-> (view [:msg-template :select :query] {:msg_template_idx msg_template_idx})
						  (first))
		  contact-list (view [:contact :select :query]
							 {:contact_group_idx contact_group_idx})
		  contact-group (-> (view [:contact-group :select :query] {:contact_group_idx contact_group_idx})
							(first))
		  contact-count (count contact-list)
		  message (sms-message [:scheduler (keyword model_id)] {:sensor-data   sensor-data
																:msg-message   msg-message
																:sms-template  sms-template
																:contact-group contact-group})]
		(timbre/debug {:sensor-data   sensor-data
					   :msg-message   msg-message
					   :sms-template  sms-template
					   :contact-group contact-group})
		(if (> contact-count 0)
			(loop [x 0]
				(if (< x contact-count)
					(do
						(send_sms_db_query message (nth contact-list x))
						(recur (inc x))))))))