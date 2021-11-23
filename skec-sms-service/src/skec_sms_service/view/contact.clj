(ns skec-sms-service.view.contact
	(:require [korma.db :as d]
			  [korma.core :as c]
			  [skec-sms-service.interface :refer :all]
			  [clj-time.core :as t]
			  [skec-sms-service.util.core :refer :all]))

(c/defentity contact
	(c/table :contact)
	(c/entity-fields :contact_idx
					 :contact_group_idx
					 :name
					 :phone_number
					 :position
					 :department))

(defn install-contact-views
	[]
	(defmethod view [:contact :select :all]
		[_ _]
		(c/select contact))

	(defmethod view [:contact :select :query]
		[_ query]
		(c/select contact
				  (c/where query)))

	(defmethod view [:contact :update]
		[_ query]
		(c/update contact
				  (c/set-fields query)
				  (c/where (select-keys query [:contact_idx]))))

	(defmethod view [:contact :insert]
		[_ query]
		(c/insert contact
				  (c/values query))))

(defn uninstall-contact-views []
	(remove-method view [:contact :select :all])
	(remove-method view [:contact :select :query])
	(remove-method view [:contact :update])
	(remove-method view [:contact :insert]))


(install-contact-views)