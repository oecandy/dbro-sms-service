(ns skec-sms-service.domain.scheduler-job
	(:require [skec-sms-service.domain.sms :as sms]
			  [skec-sms-service.interface :refer [view raw-query]]
			  [skec-sms-service.util.core :refer :all]
			  [taoensso.timbre :as timbre]))

(defn confirm-scheduler-execute
	"스케쥴 동작 시간 인지 판별"
	[{:keys [scheduler_idx
			 use_dow]
	  :as scheduler}]
	(let [formatted-now (get-formatted-time)
		  scheduler-detail (-> (view [:scheduler-dtl :select :query]
									 {:scheduler_idx scheduler_idx
									  :time          formatted-now})
							   (first))]
		(if (and (not (empty? scheduler-detail))
				 (classify-scheduled-day use_dow))
			(try
				(sms/send-schedule-message scheduler)
				(catch Exception e
					(timbre/error e))))))

(defn schedule-main-timer
	"메인 타이머 JOB"
	[]
	(let [scheduler-list (view [:scheduler :select :query] {:used "Y"})
		  scheduler-count (count scheduler-list)]
		(if (> scheduler-count 0)
			(loop [x 0]
				(if (< x scheduler-count)
					(do
						(confirm-scheduler-execute (nth scheduler-list x))
						(recur (inc x))))))))