(ns skec-sms-service.view.scheduler
	(:require [korma.db :as d]
			  [korma.core :as c]
			  [skec-sms-service.interface :refer :all]
			  [clj-time.core :as t]
			  [skec-sms-service.util.core :refer :all]))

(c/defentity scheduler
	(c/table :scheduler)
	(c/entity-fields :scheduler_idx
					 :sensor_idx
					 :sms_template_idx
					 :contact_group_idx
					 :use_dow
					 :used))

(c/defentity scheduler-dtl
	(c/table :scheduler_dtl)
	(c/entity-fields :scheduler_dtl_idx
					 :scheduler_idx
					 :time))

(defn install-scheduler-views
	[]
	(defmethod view [:scheduler :select :all]
		[_ _]
		(c/select scheduler))

	(defmethod view [:scheduler :select :query]
		[_ query]
		(c/select scheduler
				  (c/where query)))

	(defmethod view [:scheduler :update]
		[_ query]
		(c/update scheduler
				  (c/set-fields query)
				  (c/where (select-keys query [:scheduler_idx]))))

	(defmethod view [:scheduler :insert]
		[_ query]
		(c/insert scheduler
				  (c/values query)))


	(defmethod view [:scheduler-dtl :select :all]
		[_ _]
		(c/select scheduler-dtl))

	(defmethod view [:scheduler-dtl :select :query]
		[_ query]
		(c/select scheduler-dtl
				  (c/where query)))

	(defmethod view [:scheduler-dtl :update]
		[_ query]
		(c/update scheduler-dtl
				  (c/set-fields query)
				  (c/where (select-keys query [:scheduler_dtl_idx]))))

	(defmethod view [:scheduler-dtl :insert]
		[_ query]
		(c/insert scheduler-dtl
				  (c/values query))))

(defn uninstall-scheduler-views []
	(remove-method view [:scheduler-dtl :select :all])
	(remove-method view [:scheduler-dtl :select :query])
	(remove-method view [:scheduler-dtl :update])
	(remove-method view [:scheduler-dtl :insert])
	(remove-method view [:scheduler :select :all])
	(remove-method view [:scheduler :select :query])
	(remove-method view [:scheduler :update])
	(remove-method view [:scheduler :insert]))

(install-scheduler-views)