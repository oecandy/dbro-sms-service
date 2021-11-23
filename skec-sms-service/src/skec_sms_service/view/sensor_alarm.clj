(ns skec-sms-service.view.sensor-alarm
	(:require [korma.db :as d]
			  [korma.core :as c]
			  [skec-sms-service.interface :refer :all]
			  [clj-time.core :as t]
			  [skec-sms-service.util.core :refer :all]))

(c/defentity sensor-alarm
	(c/table :sensor_alarm)
	(c/entity-fields :sensor_alarm_idx
					 :sms_template_idx
					 :sensor_alarm_name
					 :sensor_idx
					 :sensor_key
					 :higher_value
					 :lower_value
					 :current_value
					 :contact_group_idx
					 :status
					 :alarm_time
					 :use_start_time
					 :use_end_time
					 :use_dow
					 :used))

(defn install-sensor-alarm-views
	[]
	(defmethod view [:sensor-alarm :select :all]
		[_ _]
		(c/select sensor-alarm))

	(defmethod view [:sensor-alarm :select :query]
		[_ query]
		(c/select sensor-alarm
				  (c/where query)))

	(defmethod view [:sensor-alarm :update]
		[_ query]
		(c/update sensor-alarm
				  (c/set-fields query)
				  (c/where (select-keys query [:sensor_alarm_idx]))))

	(defmethod view [:sensor-alarm :insert]
		[_ query]
		(c/insert sensor-alarm
				  (c/values query))))

(defn uninstall-sensor-alarm-views []
	(remove-method view [:sensor-alarm :select :all])
	(remove-method view [:sensor-alarm :select :query])
	(remove-method view [:sensor-alarm :update])
	(remove-method view [:sensor-alarm :insert]))

(install-sensor-alarm-views)