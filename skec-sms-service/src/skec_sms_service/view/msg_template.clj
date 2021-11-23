(ns skec-sms-service.view.msg-template
	(:require [korma.db :as d]
			  [korma.core :as c]
			  [skec-sms-service.interface :refer :all]
			  [clj-time.core :as t]
			  [skec-sms-service.util.core :refer :all]))

(c/defentity msg-template
	(c/table :msg_template)
	(c/entity-fields :msg_template_idx
					 :msg_template_name
					 :msg_form))

(defn install-msg-template-views
	[]
	(defmethod view [:msg-template :select :all]
		[_ _]
		(c/select msg-template))

	(defmethod view [:msg-template :select :query]
		[_ query]
		(c/select msg-template
				  (c/where query)))

	(defmethod view [:msg-template :update]
		[_ query]
		(c/update msg-template
				  (c/set-fields query)
				  (c/where (select-keys query [:msg_template_idx]))))

	(defmethod view [:msg-template :insert]
		[_ query]
		(c/insert msg-template
				  (c/values query))))

(defn uninstall-msg-template-views []
	(remove-method view [:msg-template :select :all])
	(remove-method view [:msg-template :select :query])
	(remove-method view [:msg-template :update])
	(remove-method view [:msg-template :insert]))

(install-msg-template-views)
