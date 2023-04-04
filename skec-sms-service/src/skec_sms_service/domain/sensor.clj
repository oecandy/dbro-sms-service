(ns skec-sms-service.domain.sensor
	(:require [clojure.spec.alpha :as spec]
			  [skec-sms-service.interface :refer [device-message
												  view]]
			  [skec-sms-service.util.core :as util]
			  [clj-time.core :as t]
			  [clj-time.coerce :as tc]
			  [taoensso.timbre :as timbre]
			  [skec-sms-service.domain.sms :as sms]))

(defn alarm-level-3-process
	"processing alarm level 3 plan"
	[{:keys [level_3_high_value
			 highest_value
			 higher_value
			 lower_value
			 lowest_value
			 level_3_low_value
			 status]
	  :as   sensor-alarm} sensor-value]
	(cond
		(and (> sensor-value level_3_high_value) (not= status "23"))                       ;; 3단계 초과
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "23"))
			(sms/send-alarm-message sensor-alarm sensor-value "23"))
		(and (> sensor-value highest_value) (<= sensor-value level_3_high_value) (not= status "22")) ;; 3단계 >= 센서값 > 2단계
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "22"))
			(sms/send-alarm-message sensor-alarm sensor-value "22"))
		(and (> sensor-value higher_value) (<= sensor-value highest_value) (not= status "21")) ;; 2단계 >= 센서값 > 1단계
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "21"))
			(sms/send-alarm-message sensor-alarm sensor-value "21"))
		(and (<= sensor-value higher_value) (>= sensor-value lower_value))
		(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
														  :status "10"))
		(and (< sensor-value lower_value) (>= sensor-value lowest_value) (not= status "01")) ;; 1단계 >= 센서값 >= 1단계 low
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "01"))
			(sms/send-alarm-message sensor-alarm sensor-value "01"))
		(and (< sensor-value lowest_value) (>= sensor-value level_3_low_value) (not= status "02"))
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "02"))
			(sms/send-alarm-message sensor-alarm sensor-value "02"))
		(and (< sensor-value level_3_low_value) (not= status "03"))
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "03"))
			(sms/send-alarm-message sensor-alarm sensor-value "03"))))

(defn alarm-level-2-process
	"processing alarm level 2 plan"
	[{:keys [highest_value
			 higher_value
			 lower_value
			 lowest_value]
	  :as   sensor-alarm} sensor-value]
	(cond
		(> sensor-value highest_value)
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "22"))
			(sms/send-alarm-message sensor-alarm sensor-value "22"))
		(and (> sensor-value higher_value) (<= sensor-value highest_value))
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "21"))
			(sms/send-alarm-message sensor-alarm sensor-value "21"))
		(and (<= sensor-value higher_value) (>= sensor-value lower_value))
		(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
														  :status "10"))
		(and (< sensor-value lower_value) (>= sensor-value lowest_value))
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "01"))
			(sms/send-alarm-message sensor-alarm sensor-value "01"))
		(< sensor-value lowest_value)
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "02"))
			(sms/send-alarm-message sensor-alarm sensor-value "02"))))

(defn alarm-level-1-process
	"processing alarm level 1 plan"
	[{:keys [higher_value
			 lower_value]
	  :as   sensor-alarm} sensor-value]
	(cond
		(> sensor-value higher_value)
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "21"))
			(sms/send-alarm-message sensor-alarm sensor-value "21"))
		(and (<= sensor-value higher_value) (>= sensor-value lower_value))
		(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
														  :status "10"))
		(< sensor-value lower_value)
		(do
			(view [:sensor-alarm :update] (assoc sensor-alarm :current_value sensor-value
															  :alarm_time (tc/to-sql-time (t/now))
															  :status "01"))
			(sms/send-alarm-message sensor-alarm sensor-value "01"))))

(defn validate-sensor-alarm
	"validate sensor value with sensor alarm value"
	[payload-info sensor-alarm]
	(let [{:keys [sensor_key
				  higher_value
				  highest_value
				  lower_value
				  lowest_value
				  alarm_time
				  use_start_time
				  use_end_time
				  use_dow
				  interval
				  alarm_level]} sensor-alarm
		  sensor-value ((keyword sensor_key) payload-info)
		  formatted-time (util/get-formatted-time)]
		(try
			(if (and (t/after? (t/now) (t/plus (tc/from-sql-time alarm_time) (t/minutes interval)))
					 (util/classify-scheduled-day use_dow)
					 (or (or (empty? use_start_time) (empty? use_end_time))
						 (and (<= (Integer/parseInt use_start_time) formatted-time)
							  (>= (Integer/parseInt use_end_time) formatted-time))))
				(case alarm_level
					1 (alarm-level-1-process sensor-alarm sensor-value)
					2 (alarm-level-2-process sensor-alarm sensor-value)
					3 (alarm-level-3-process sensor-alarm sensor-value)))
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