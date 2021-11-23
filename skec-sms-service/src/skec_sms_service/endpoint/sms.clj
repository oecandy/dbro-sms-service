(ns skec-sms-service.endpoint.sms
	(:require [taoensso.timbre :as timbre]
			  [skec-sms-service.interface :refer [view
												  raw-query]]
			  [ring.util.response :as r]
			  [clojure.spec.alpha :as s]
			  [clojure.spec.gen.alpha :as g]
			  [clj-time.core :as t]
			  [clj-time.coerce :as tc]
			  [failjure.core :as fail]
			  [cheshire.generate :as gen]
			  [skec-sms-service.util.core :as util]
			  [skec-sms-service.domain.sms :as sms]))

; msg template parameters
(s/def ::x-authorization string?)
(s/def ::msg_template_idx number?)
(s/def ::msg_template_name string?)
(s/def ::msg_form string?)

; sms template parameters
(s/def ::sms_template_idx number?)
(s/def ::sms_template_name string?)
(s/def ::sensor_type_idx number?)
(s/def ::over_state number?)
(s/def ::under_state number?)
(s/def ::hyper_link_url string?)
(s/def ::unit string?)

; sending sms parameters
(s/def ::phone_number string?)
(s/def ::message string?)

(defn handle-select-message-template-list
	"메세지 템플릿 리스트 조회"
	[{{{:keys [x-authorization]} :header} :parameters}]
	(let [result (view [:msg-template :select :all] {})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-select-message-template
	"메세지 템플릿 리스트 조회"
	[{{{:keys [x-authorization]}  :header
	   {:keys [msg_template_idx]} :path} :parameters}]
	(let [result (-> (view [:msg-template :select :query] {:msg_template_idx msg_template_idx})
					 (first))]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-create-message-template
	"메세지 템플릿 생성"
	[{{{:keys [x-authorization]} :header
	   {:keys [msg_template_name
			   msg_form]}        :body} :parameters}]
	(let [result (view [:msg-template :insert] {:msg_template_name msg_template_name
												:msg_form          msg_form})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-update-message-template
	"메세지 템플릿 리스트 조회"
	[{{{:keys [x-authorization]}  :header
	   {:keys [msg_template_idx]} :path
	   {:keys [msg_template_name
			   msg_form]}         :body} :parameters}]
	(let [result (view [:msg-template :update] {:msg_template_idx  msg_template_idx
												:msg_template_name msg_template_name
												:msg_form          msg_form})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-select-sms-template-list
	"메세지 템플릿 리스트 조회"
	[{{{:keys [x-authorization]} :header} :parameters}]
	(let [result (view [:sms-template :select :all] {})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-select-sms-template
	"메세지 템플릿 리스트 조회"
	[{{{:keys [x-authorization]}  :header
	   {:keys [sms_template_idx]} :path} :parameters}]
	(let [result (-> (view [:sms-template :select :query] {:sms_template_idx sms_template_idx})
					 (first))]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-create-sms-template
	"메세지 템플릿 리스트 조회"
	[{{{:keys [x-authorization]} :header
	   {:keys [msg_template_idx
			   sms_template_name
			   over_state
			   under_state
			   hyper_link_url
			   unit]}            :body} :parameters}]
	(let [result (view [:sms-template :insert] {:msg_template_idx  msg_template_idx
												:sms_template_name sms_template_name
												:over_state        over_state
												:under_state       under_state
												:hyper_link_url    hyper_link_url
												:unit              unit})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-update-sms-template
	"메세지 템플릿 리스트 조회"
	[{{{:keys [x-authorization]}  :header
	   {:keys [sms_template_idx]} :path
	   {:keys [msg_template_idx
			   sms_template_name
			   over_state
			   under_state
			   hyper_link_url
			   unit]}             :body} :parameters}]
	(let [result (view [:sms-template :update] {:sms_template_idx  sms_template_idx
												:sms_template_name sms_template_name
												:msg_template_idx  msg_template_idx
												:over_state        over_state
												:under_state       under_state
												:hyper_link_url    hyper_link_url
												:unit              unit})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-sending-sms
	"메세지 템플릿 리스트 조회"
	[{{{:keys [x-authorization]} :header
	   {:keys [message
			   phone_number]}    :body} :parameters}]
	(let [result (if (> 90 (util/get-string-byte-length message))
					 (view [:sms :insert] {:TRAN_PHONE    phone_number
										   :TRAN_CALLBACK "0261192221"
										   :TRAN_STATUS   "1"
										   :TRAN_DATE     (tc/to-sql-time (t/now))
										   :TRAN_TYPE     4
										   :TRAN_MSG      message})
					 (sms/send_mms_db_query message {:phone_number phone_number}))]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(def routes ["/sms" {:swagger {:tags ["SMS endpoint"]}}
			 [""
			  ["/send"
			   [""
				{:post {:summary     "일반 메세지 보내기"
						:description ""
						:parameters  {:body (s/keys :req-un [::phone_number
															 ::message])}
						:handler     handle-sending-sms}}]]
			  ["/msg"
			   [""
				{:get  {:summary     "메세지 템플릿 리스트 조회"
						:description ""
						:parameters  {}
						:handler     handle-select-message-template-list}
				 :post {:summary     "메세지 템플릿 리스트 생성"
						:description ""
						:parameters  {:body (s/keys :req-un [::msg_template_name
															 ::msg_form])}
						:handler     handle-create-message-template}}]
			   ["/:msg_template_idx"
				{:get {:summary     "메세지 템플릿 상세 조회"
					   :description (str "")
					   :parameters  {:path (s/keys :req-un [::msg_template_idx])}
					   :handler     handle-select-message-template}
				 :put {:summary     "메세지 템플릿 수정"
					   :description (str "")
					   :parameters  {:path (s/keys :req-un [::msg_template_idx])
									 :body (s/keys :req-un [::msg_template_name
															::msg_form])}
					   :handler     handle-update-message-template}}]]
			  ["/template"
			   [""
				{:get  {:summary     "센서 알람 템플릿 리스트 조회"
						:description (str "")
						:parameters  {}
						:handler     handle-select-sms-template-list}
				 :post {:summary     "센서 알람 템플릿 리스트 생성"
						:description (str "")
						:parameters  {:body (s/keys :req-un [::msg_template_idx
															 ::sms_template_name
															 ::over_state
															 ::under_state
															 ::hyper_link_url
															 ::unit])}
						:handler     handle-create-sms-template}}]
			   ["/:sms_template_idx"
				{:get {:summary     "센서 알람 템플릿 리스트 조회"
					   :description (str "")
					   :parameters  {:path (s/keys :req-un [::sms_template_idx])}
					   :handler     handle-select-sms-template}
				 :put {:summary     "센서 알람 템플릿 리스트 조회"
					   :description (str "")
					   :parameters  {:path (s/keys :req-un [::sms_template_idx])
									 :body (s/keys :req-un [::msg_template_idx
															::sms_template_name
															::over_state
															::under_state
															::hyper_link_url
															::unit])}
					   :handler     handle-update-sms-template}}]]]])
