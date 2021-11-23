(ns skec-sms-service.view.sms-template
	(:require [korma.db :as d]
			  [korma.core :as c]
			  [skec-sms-service.interface :refer :all]
			  [clj-time.core :as t]
			  [skec-sms-service.util.core :refer :all]))

(c/defentity sms-template
	(c/table :sms_template)
	(c/entity-fields :sms_template_idx
					 :msg_template_idx
					 :sms_template_name
					 :sensor_type_name
					 :over_state
					 :under_state
					 :hyper_link_url
					 :unit))

(defn install-sms-template-views
	[]
	(defmethod view [:sms-template :select :all]
		[_ _]
		(c/select sms-template))

	(defmethod view [:sms-template :select :query]
		[_ query]
		(c/select sms-template
				  (c/where query)))

	(defmethod view [:sms-template :update]
		[_ query]
		(c/update sms-template
				  (c/set-fields query)
				  (c/where (select-keys query [:sms_template_idx]))))

	(defmethod view [:sms-template :insert]
		[_ query]
		(c/insert sms-template
				  (c/values query))))

(defn uninstall-sms-template-views []
	(remove-method view [:sms-template :select :all])
	(remove-method view [:sms-template :select :query])
	(remove-method view [:sms-template :update])
	(remove-method view [:sms-template :insert]))

(install-sms-template-views)