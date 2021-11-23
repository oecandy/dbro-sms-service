(ns skec-sms-service.endpoint.core
	(:require [org.httpkit.server :as server]
			  [reitit.ring :as ring]
			  [reitit.swagger :as swagger]
			  [reitit.swagger-ui :as swagger-ui]
			  [reitit.coercion.spec :as spec-coercion]
			  [reitit.ring.middleware.parameters :as parameters]
			  [reitit.ring.middleware.multipart :as multipart]
			  [ring.middleware.json :as ring-json]
			  [reitit.ring.coercion :as ring-coercion]
			  [reitit.ring.middleware.muuntaja :as muuntaja]
			  [ring.util.http-response :as response]
			  [ring.middleware.params :as params]
			  [ring.middleware.session :as session]
			  [muuntaja.core :as m]
			  [clojure.pprint :as pp]
			  [clojure.string :as str]
			  [clojure.data.json :refer [read-json]]
			  [schema.core :as s]
			  [reitit.coercion.schema]
			  [reitit.coercion.spec]
			  [taoensso.timbre :as timbre]
			  [reitit.ring.middleware.exception :as exception]
			  [taoensso.timbre :as timbre]
			  [reitit.http.interceptors.muuntaja]
			  [clojure.spec.alpha :as spec]
			  [ring.util.response :as r]
			  [skec-sms-service.interface :refer [*system*]]
			  [skec-sms-service.endpoint.contact :as contact]
			  [skec-sms-service.endpoint.sensor :as sensor]
			  [skec-sms-service.endpoint.sms :as sms]))

(spec/def ::access (spec/keys :req-un [::auth ::role ::platform]))

(defn exception-handler [message exception request]
	(timbre/error (.getLocalizedMessage exception))
	(timbre/error (timbre/stacktrace exception))
	{:status 500
	 :body   {:message   message
			  :exception (.getClass exception)
			  :data      (ex-data exception)
			  :uri       (:uri request)}})

(def exception-middleware
	(exception/create-exception-middleware
		(merge
			exception/default-handlers
			{;; ex-data with :type ::error
			 ::error               (partial exception-handler "error")

			 ;; ex-data with ::exception or ::failure
			 ::exception           (partial exception-handler "exception")

			 ;; SQLException and all it's child classes
			 java.sql.SQLException (partial exception-handler "sql-exception")

			 ;; override the default handler
			 ::exception/default   (partial exception-handler "default")

			 ;; print stack-traces for all exceptions
			 ::exception/wrap      (fn [handler e request]
									   (println "ERROR" (pr-str (:uri request)))
									   (handler e request))})))

(defn api-accessible?
	[{:keys [uri body-params parameters headers request-method] :as request}]
	(let [is-preflight-request? (= :options request-method)]
		(timbre/debug (str "uri :: " uri))
		;(timbre/debug (str "headers :: " headers))
		(cond
			is-preflight-request?
			{:accessible? true
			 :message     "."
			 :status-code 200}
			:else
			(or (re-matches #"/swagger.json" uri)
				(re-matches #"/api-docs/.*" uri)
				{:accessible? true
				 :message     "."
				 :status-code 200}))))

(def Cors-options
	{"Access-Control-Allow-Origin"      "*"
	 "Access-Control-Allow-Methods"     "GET,POST,PUT,DELETE,OPTIONS"
	 "Access-Control-Allow-Credentials" "true"
	 "Access-Control-Allow-Headers"     "X-Authorization,Content-Type,xsrf-token,Origin,Accept,X-Requested-With,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization"
	 "Access-Control-Expose-Headers"    "X-Authorization"
	 "Access-Control-Max-Age"           "3600"})

;; jwt-token이 있을 경우, request-register는 불가능함
(defn check-auth [handler]
	(fn [request]
		(timbre/debug "checking auth...")
		(let [{:keys [message
					  accessible?
					  status-code]} (api-accessible? request)]
			(if accessible?
				(handler request)
				{:status  status-code
				 :body    message
				 :headers Cors-options}))))

(defn cors-middleware [handler]
	(fn [request]
		(let [response (handler request)]
			(timbre/debug response)
			(update response :headers
					#(into % Cors-options)))))

(defn wrap-routes-data
	"Swagger에 표시될 router의 접근을 위해 파라미터를 추가합니다.
	routes: 라우터 경로 모음입니다. ring/router의 파라미터로 쓰입니다.
	api-excepted: 래핑을 제외할 경로입니다. 말단 경로입니다.
	ex) #{\"/api/account/register\"}
	access-keys: 변경할 파라미터의 키모음으로 시퀀스로 쓰입니다.
	ex) [:body :email] 또는 [:header :access-token]
	pred-fn: 체크할 함수입니다.
	ex) string? int?
	예제는 다음과 같습니다.
	ex) (wrap-routes-data route-data #{\"/email-authentication\" \"/swagger.json\"})"
	([routes api-excepted access-keys pred-fn]
	 (cond
		 (string? routes)
		 routes
		 (vector? routes)
		 (mapv #(wrap-routes-data % api-excepted access-keys pred-fn "") routes)
		 :else routes))
	([routes api-excepted [method-name-key &
						   sub-keys :as query-keys] pred-fn full-path]
	 (let [access-keys (into [:parameters method-name-key] sub-keys)]
		 (cond
			 (string? routes) routes
			 (map? routes) routes
			 (vector? routes)
			 (mapv #(wrap-routes-data % api-excepted query-keys pred-fn
									  (str full-path (first routes))) routes)
			 (and (map? (second routes))
				  (not (api-excepted (str full-path (first routes)))))
			 (let [[route {:keys [post get put delete] :as methods} & sub-routes] routes
				   method-update (fn [m k v]
									 (let [method-val (get-in v [:parameters method-name-key])
										   v (if (and (#{:post :get :put :delete} k)
													  (or (nil? method-val) (map? method-val)))
												 (assoc-in v access-keys pred-fn) v)]
										 (assoc m k v)))
				   updated-methods (reduce-kv method-update {} methods)
				   route-updated [route updated-methods]]
				 (if (seq sub-routes)
					 (conj route-updated
						   (mapv #(wrap-routes-data
									  % api-excepted
									  query-keys
									  pred-fn
									  (str full-path (first routes))) sub-routes))
					 route-updated))
			 :else routes))))



(def data
	[["/swagger.json" {:get {:no-doc  true
							 :swagger {:info {:title "SKEC SMS service core"}}
							 :handler (swagger/create-swagger-handler)}}]
	 ["/api-docs/*" {:get {:no-doc  true
						   :handler (swagger-ui/create-swagger-ui-handler)}}]
	 ["/api" {:middleware [[check-auth] [cors-middleware]]}
	  sms/routes
	  sensor/routes
	  contact/routes]])

(def wrapped-data
	(-> data
		(wrap-routes-data #{"/swagger.json"} [:header :x-authorization] string?)))

(def app
	(ring/ring-handler
		(ring/router
			wrapped-data
			{:data {:coercion   reitit.coercion.spec/coercion
					:muuntaja   m/instance
					:middleware [swagger/swagger-feature
								 params/wrap-params
								 ring-json/wrap-json-response
								 muuntaja/format-negotiate-middleware
								 muuntaja/format-response-middleware                ;; encoding response body
								 muuntaja/format-request-middleware                 ;; decoding request body
								 ring-coercion/coerce-exceptions-middleware
								 ring-coercion/coerce-request-middleware            ;;coercing response bodys
								 ring-coercion/coerce-response-middleware
								 multipart/multipart-middleware
								 exception-middleware]}})                           ;;decoding request body
		(ring/routes
			(swagger-ui/create-swagger-ui-handler {:path "/"})
			(ring/create-default-handler))))