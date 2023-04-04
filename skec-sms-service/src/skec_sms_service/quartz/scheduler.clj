(ns skec-sms-service.quartz.scheduler
	(:require [clojurewerkz.quartzite.scheduler :as qs]
			  [clojurewerkz.quartzite.triggers :as t]
			  [clojurewerkz.quartzite.jobs :as j]
			  [clojurewerkz.quartzite.schedule.cron :refer [schedule cron-schedule]]
			  [skec-sms-service.domain.scheduler-job :as job]
			  [taoensso.timbre :as timbre]))


(j/defjob timer-job
		[ctx]
		(timbre/info "===================================================")
		(timbre/info "Start scheduled job :: timer")
		(job/schedule-main-timer)
		(timbre/info "==================================================="))

(defn- scheduler-builder
	[schduler {:keys [expression
					  job-key
					  trigger-key]}]
	(let [job-type (case job-key
					   "jobs.timer.1" timer-job
					   timer-job)
		  job (j/build
				  (j/of-type job-type)
				  (j/with-identity (j/key job-key)))
		  trigger (t/build
					  (t/with-identity (t/key trigger-key))
					  (t/start-now)
					  (t/with-schedule (schedule
										   (cron-schedule expression))))]
		(qs/schedule schduler job trigger)))

(defn scheduler-core
	"모든 스케듈러 제어 코어"
	[{:keys [main-timer]}]
	(let [s (-> (qs/initialize) qs/start)]
		(do
			(scheduler-builder s main-timer)
			s)))