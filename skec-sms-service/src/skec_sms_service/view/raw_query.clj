(ns skec-sms-service.view.raw-query
	(:require [korma.db :as d]
			  [korma.core :as c]
			  [skec-sms-service.interface :refer :all]
			  [clj-time.core :as t]
			  [skec-sms-service.util.core :refer :all]))

(def select-all-sms-message-info
	(str
		"SELECT A.sensor_alarm_idx, A.current_value, A.status, "
		"B.unit, B.sensor_type_name, B.level_3_over_state, B.over_state, B.under_state, B.max_state, B.min_state, B.level_3_under_state,"
		"B.hyper_link_url, C.msg_form, D.group_name "
		"FROM sensor_alarm A "
		"LEFT OUTER JOIN sms_template B ON(A.sms_template_idx = B.sms_template_idx) "
		"LEFT OUTER JOIN msg_template C ON(B.msg_template_idx = C.msg_template_idx) "
		"LEFT OUTER JOIN contact_group D ON(A.contact_group_idx = D.contact_group_idx) "))


(defn install-raw-queries
	[]
	(defmethod raw-query [:select :sms-message :sensor-alarm-idx]
		[_ {:keys [sensor_alarm_idx]}]
		(let [full-query (str select-all-sms-message-info
							  " WHERE A.sensor_alarm_idx=?")]
			(c/exec-raw [full-query [sensor_alarm_idx]] :results))))

(defn uninstall-sms-template-views []
	(remove-method raw-query [:select :sms-message :sensor-alarm-idx]))

(install-raw-queries)