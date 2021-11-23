(ns skec-sms-service.endpoint.contact
	(:require [taoensso.timbre :as timbre]
			  [skec-sms-service.interface :refer [view
												  raw-query]]
			  [ring.util.response :as r]
			  [clojure.spec.alpha :as s]
			  [clojure.spec.gen.alpha :as g]
			  [clojure.spec.alpha :as spec]
			  [clojure.data.json :as json]
			  [failjure.core :as fail]
			  [cheshire.generate :as gen]
			  [skec-sms-service.util.core :as util]))

(s/def ::x-authorization string?)

(s/def ::contact_group_idx number?)
(s/def ::group_name string?)

(s/def ::contact_idx number?)
(s/def ::name string?)
(s/def ::phone_number string?)
(s/def ::position string?)
(s/def ::department string?)

(defn handle-select-all-contact-list
	"모든 연락처 조회"
	[{{{:keys [x-authorization]} :header} :parameters}]
	(let [result (view [:contact :select :all] {})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-select-all-contact-group-list
	"연락처 그룹 리스트 조회"
	[{{{:keys [x-authorization]} :header} :parameters}]
	(let [result (view [:contact-group :select :all] {})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-create-contact-group
	"연락처 그룹 생성"
	[{{{:keys [x-authorization]} :header
	   {:keys [group_name]}      :body} :parameters}]
	(let [result (view [:contact-group :insert] {:group_name group_name})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-select-group-all-contact-list
	"특정 그룹 연락처 리스트 조회"
	[{{{:keys [x-authorization]}   :header
	   {:keys [contact_group_idx]} :path} :parameters}]
	(let [result (view [:contact :select :query] {:contact_group_idx contact_group_idx})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-create-contact
	"연락처 생성"
	[{{{:keys [x-authorization]}   :header
	   {:keys [contact_group_idx]} :path
	   {:keys [name
			   phone_number
			   position
			   department]}        :body} :parameters}]
	(let [result (view [:contact :insert] {:contact_group_idx contact_group_idx
										   :name              name
										   :phone_number      phone_number
										   :position          position
										   :department        department})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-update-contact-group
	"연락처 그룹 정보 수정"
	[{{{:keys [x-authorization]}   :header
	   {:keys [contact_group_idx]} :path
	   {:keys [group_name]}        :body} :parameters}]
	(let [result (view [:contact-group :update] {:contact_group_idx contact_group_idx
												 :group_name        group_name})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-select-contact-info
	"연락처 정보 조회"
	[{{{:keys [x-authorization]} :header
	   {:keys [contact_group_idx
			   contact_idx]}     :path} :parameters}]
	(let [result (view [:contact :select :query] {:contact_idx contact_idx})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(defn handle-update-contact-info
	"연락처 정보 수정"
	[{{{:keys [x-authorization]} :header
	   {:keys [contact_group_idx
			   contact_idx]}     :path
	   {:keys [name
			   phone_number
			   position
			   department]}      :body} :parameters}]
	(let [result (view [:contact :update] {:contact_idx       contact_idx
										   :contact_group_idx contact_group_idx
										   :name              name
										   :phone_number      phone_number
										   :position          position
										   :department        department})]
		(-> (r/response {:result result})
			(r/content-type "application/json"))))

(def routes ["/contact" {:swagger {:tags ["Contact endpoint"]}}
			 [""
			  {:get {:summary     "모든 연락처 조회"
					 :description (str "")
					 :parameters  {}
					 :handler     handle-select-all-contact-list}}]
			 ["/group"
			  [""
			   {:get  {:summary     "연락처 그룹 리스트 조회"
					   :description (str "")
					   :parameters  {}
					   :handler     handle-select-all-contact-group-list}
				:post {:summary     "연락처 그룹 생성"
					   :description (str "")
					   :parameters  {:body (s/keys :req-un [::group_name])}
					   :handler     handle-create-contact-group}}]
			  ["/:contact_group_idx"
			   [""
				{:get  {:summary     "특정 그룹 연락처 리스트 조회"
						:description (str "")
						:parameters  {:path (s/keys :req-un [::contact_group_idx])}
						:handler     handle-select-group-all-contact-list}
				 :post {:summary     "연락처 생성"
						:description (str "")
						:parameters  {:path (s/keys :req-un [::contact_group_idx])
									  :body (s/keys :req-un [::name
															 ::phone_number
															 ::position
															 ::department])}
						:handler     handle-create-contact}
				 :put  {:summary     "연락처 그룹 정보 수정"
						:description (str "")
						:parameters  {:path (s/keys :req-un [::contact_group_idx])
									  :body (s/keys :req-un [::group_name])}
						:handler     handle-update-contact-group}}]
			   ["/:contact_idx"
				{:get {:summary     "연락처 정보 조회"
					   :description (str "")
					   :parameters  {:path (s/keys :req-un [::contact_group_idx
															::contact_idx])}
					   :handler     handle-select-contact-info}
				 :put {:summary     "연락처 정보 수정"
					   :description (str "")
					   :parameters  {:path (s/keys :req-un [::contact_group_idx
															::contact_idx])
									 :body (s/keys :req-un [::name
															::phone_number
															::position
															::department])}
					   :handler     handle-update-contact-info}}]]]])

