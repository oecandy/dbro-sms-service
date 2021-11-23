(ns skec-sms-service.view.sensor
	(:require [korma.db :as d]
			  [korma.core :as c]
			  [skec-sms-service.interface :refer :all]
			  [clj-time.core :as t]
			  [skec-sms-service.util.core :refer :all]))

(c/defentity sensor
	(c/table :sensor)
	(c/entity-fields :sensor_idx
					 :hardware_type
					 :network_type
					 :model_id
					 :serial_no))

(defn install-sensor-views
	[]
	(defmethod view [:sensor :select :all]
		[_ _]
		(c/select sensor))

	(defmethod view [:sensor :select :query]
		[_ query]
		(c/select sensor
				  (c/where query)))

	(defmethod view [:sensor :update]
		[_ query]
		(c/update sensor
				  (c/set-fields query)
				  (c/where (select-keys query [:sensor_idx]))))

	(defmethod view [:sensor :insert]
		[_ query]
		(c/insert sensor
				  (c/values query))))

(defn uninstall-sensor-views []
	(remove-method view [:sensor :select :all])
	(remove-method view [:sensor :select :query])
	(remove-method view [:sensor :update])
	(remove-method view [:sensor :insert]))

(install-sensor-views)