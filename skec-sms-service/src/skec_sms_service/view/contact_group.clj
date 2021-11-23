(ns skec-sms-service.view.contact-group
	(:require [korma.db :as d]
			  [korma.core :as c]
			  [skec-sms-service.interface :refer :all]
			  [clj-time.core :as t]
			  [skec-sms-service.util.core :refer :all]))

(c/defentity contact-group
	(c/table :contact_group)
	(c/entity-fields :contact_group_idx
					 :group_name))

(defn install-contact-group-views
	[]
	(defmethod view [:contact-group :select :all]
		[_ _]
		(c/select contact-group))

	(defmethod view [:contact-group :select :query]
		[_ query]
		(c/select contact-group
				  (c/where query)))

	(defmethod view [:contact-group :update]
		[_ query]
		(c/update contact-group
				  (c/set-fields query)
				  (c/where (select-keys query [:contact_group_idx]))))

	(defmethod view [:contact-group :insert]
		[_ query]
		(c/insert contact-group
				  (c/values query))))

(defn uninstall-contact-group-views []
	(remove-method view [:contact-group :select :all])
	(remove-method view [:contact-group :select :query])
	(remove-method view [:contact-group :update])
	(remove-method view [:contact-group :insert]))

(install-contact-group-views)