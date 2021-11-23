(ns skec-sms-service.view.sms
	(:require [korma.db :as d]
			  [korma.core :as c]
			  [skec-sms-service.interface :refer :all]
			  [clj-time.core :as t]
			  [skec-sms-service.util.core :refer :all]))

(c/defentity em-tran
	(c/table :dbro.EM_TRAN)
	(c/entity-fields :TRAN_PR
					 :TRAN_REFKEY
					 :TRAN_ID
					 :TRAN_PHONE
					 :TRAN_CALLBACK
					 :TRAN_STATUS
					 :TRAN_DATE
					 :TRAN_RSLTDATE
					 :TRAN_REPORTDATE
					 :TRAN_RSLT
					 :TRAN_NET
					 :TRAN_MSG
					 :TRAN_ETC1
					 :TRAN_ETC2
					 :TRAN_ETC3
					 :TRAN_ETC4
					 :TRAN_TYPE))

(c/defentity em-tran-kko
	(c/table :dbro.EM_TRAN_KKO)
	(c/entity-fields :KKO_SEQ
					 :SENDER_KEY
					 :TEMPLATE_CODE
					 :NATION_CODE
					 :MESSAGE
					 :RE_PART
					 :RE_TYPE
					 :RE_BODY
					 :RE_SUBJECT
					 :USER_KEY
					 :AD_FLAG
					 :RESPONSE_METHOD
					 :TIMEOUT
					 :ATTACHED_FILE_1
					 :ATTACHED_FILE_2
					 :ATTACHED_FILE_3
					 :ATTACHED_FILE_4
					 :ATTACHED_FILE_5))

(c/defentity em-tran-mms
	(c/table :dbro.EM_TRAN_MMS)
	(c/entity-fields :MMS_SEQ
					 :FILE_CNT
					 :BUILD_YN
					 :MMS_BODY
					 :MMS_SUBJECT
					 :FILE_TYPE1
					 :FILE_TYPE2
					 :FILE_TYPE3
					 :FILE_TYPE4
					 :FILE_TYPE5
					 :FILE_NAME1
					 :FILE_NAME2
					 :FILE_NAME3
					 :FILE_NAME4
					 :FILE_NAME5
					 :SERVICE_DEP1
					 :SERVICE_DEP2
					 :SERVICE_DEP3
					 :SERVICE_DEP4
					 :SERVICE_DEP5
					 :SKN_FILE_NAME))

(c/defentity em-tran-rcs
	(c/table :dbro.EM_TRAN_RCS)
	(c/entity-fields :RCS_SEQ
					 :COPY_ALLOWED
					 :CHATBOT_ID
					 :FOOTER
					 :HEADER
					 :MESSAGEBASE_ID
					 :BUTTONS
					 :RCS_BODY
					 :AGENCY_ID
					 :RE_PART
					 :RE_TYPE
					 :RE_BODY
					 :RE_SUBJECT
					 :ATTACHED_FILE_1
					 :ATTACHED_FILE_2
					 :ATTACHED_FILE_3
					 :ATTACHED_FILE_4
					 :ATTACHED_FILE_5))

(defn install-sms-views
	[]
	;; sms
	(defmethod view [:sms :select :all]
		[_ _]
		(c/select em-tran))

	(defmethod view [:sms :select :query]
		[_ query]
		(c/select em-tran
				  (c/where query)))

	(defmethod view [:sms :update]
		[_ query]
		(c/update em-tran
				  (c/set-fields query)
				  (c/where (select-keys query [:TRAN_PR]))))

	(defmethod view [:sms :insert]
		[_ query]
		(c/insert em-tran
				  (c/values query)))

	;; kakao
	(defmethod view [:kakao :select :all]
		[_ _]
		(c/select em-tran-kko))

	(defmethod view [:kakao :select :query]
		[_ query]
		(c/select em-tran-kko
				  (c/where query)))

	(defmethod view [:kakao :update]
		[_ query]
		(c/update em-tran-kko
				  (c/set-fields query)
				  (c/where (select-keys query [:KKO_SEQ]))))

	(defmethod view [:kakao :insert]
		[_ query]
		(c/insert em-tran-kko
				  (c/values query)))

	;; mms
	(defmethod view [:mms :select :all]
		[_ _]
		(c/select em-tran-mms))

	(defmethod view [:mms :select :query]
		[_ query]
		(c/select em-tran-mms
				  (c/where query)))

	(defmethod view [:mms :update]
		[_ query]
		(c/update em-tran-mms
				  (c/set-fields query)
				  (c/where (select-keys query [:MMS_SEQ]))))

	(defmethod view [:mms :insert]
		[_ query]
		(c/insert em-tran-mms
				  (c/values query)))

	;; rcs
	(defmethod view [:rcs :select :all]
		[_ _]
		(c/select em-tran-rcs))

	(defmethod view [:rcs :select :query]
		[_ query]
		(c/select em-tran-rcs
				  (c/where query)))

	(defmethod view [:rcs :update]
		[_ query]
		(c/update em-tran-rcs
				  (c/set-fields query)
				  (c/where (select-keys query [:RCS_SEQ]))))

	(defmethod view [:rcs :insert]
		[_ query]
		(c/insert em-tran-rcs
				  (c/values query))))

(defn uninstall-sms-views []
	(remove-method view [:sms :select :all])
	(remove-method view [:sms :select :query])
	(remove-method view [:sms :update])
	(remove-method view [:sms :insert])

	(remove-method view [:kakao :select :all])
	(remove-method view [:kakao :select :query])
	(remove-method view [:kakao :update])
	(remove-method view [:kakao :insert])

	(remove-method view [:mms :select :all])
	(remove-method view [:mms :select :query])
	(remove-method view [:mms :update])
	(remove-method view [:mms :insert])

	(remove-method view [:rcs :select :all])
	(remove-method view [:rcs :select :query])
	(remove-method view [:rcs :update])
	(remove-method view [:rcs :insert]))

(install-sms-views)
