(ns skec-sms-service.domain.sensor
	(:require [clojure.spec.alpha :as spec]
			  [skec-sms-service.interface :refer [device-message
												  view]]
			  [skec-sms-service.util.core :as util]
			  [clj-time.core :as t]
			  [clj-time.coerce :as tc]
			  [taoensso.timbre :as timbre]
			  [skec-sms-service.domain.sms :as sms]))

(defn validate-sensor-alarm
	"validate sensor value with sensor alarm value"
	[payload-info sensor-alarm]
	(let [{:keys [sensor_key
				  higher_value
				  lower_value
				  alarm_time
				  use_start_time
				  use_end_time
				  use_dow]} sensor-alarm
		  sensor-value ((keyword sensor_key) payload-info)
		  formatted-time (util/get-formatted-time)]
		(try
			(if (and (t/after? (t/now) (t/plus (tc/from-sql-time alarm_time) (t/minutes 30)))
					 (util/classify-scheduled-day use_dow)
					 (or (or (empty? use_start_time) (empty? use_end_time))
						 (and (<= (Integer/parseInt use_start_time) formatted-time)
							  (>= (Integer/parseInt use_end_time) formatted-time))))
				(cond
					(> sensor-value higher_value)
					(do
						(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
																		  :alarm_time (tc/to-sql-time (t/now))
																		  :status "2"))
						(sms/send-alarm-message sensor-alarm sensor-value "2"))
					(and (<= sensor-value higher_value) (>= sensor-value lower_value))
					(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
																	  :status "1"))
					(< sensor-value lower_value)
					(do
						(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
																		  :alarm_time (tc/to-sql-time (t/now))
																		  :status "0"))
						(sms/send-alarm-message sensor-alarm sensor-value "0"))))
			(catch Exception e
				(timbre/debug e)))))

(defn process-sk-sensor
	"processing sk sensor job"
	[device-map payload-info]
	(let [{:keys [sensor_idx]} device-map
		  sensor-alarm-list (view [:sensor-alarm :select :query] {:sensor_idx sensor_idx
																  :used       "Y"})
		  alarm-count (count sensor-alarm-list)]
		(if (> alarm-count 0)
			(loop [x 0]
				(if (< x alarm-count)
					(do
						(validate-sensor-alarm payload-info (nth sensor-alarm-list x))
						(recur (inc x))))))))