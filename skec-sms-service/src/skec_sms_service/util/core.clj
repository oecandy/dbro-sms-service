(ns skec-sms-service.util.core
	(:require [clojure.data.json :refer [write-str read-json]]
			  [clj-time.coerce :as tc]
			  [clj-time.core :as t]
			  [clj-time.format :as tf]
			  [clojure.pprint :refer (cl-format)]
			  [clojure.string :as str]))

(def korea-time-formatter
	(tf/formatter (t/time-zone-for-id "Asia/Seoul") "HHmm" "YYYY/MM/dd"))

(defn get-formatted-time
	"현재 시간 포맷 형태로 변환하여 리턴"
	[]
	(-> (tf/unparse korea-time-formatter (t/now))
		(Integer/parseInt)))

(defn encode-binary [decimal]
	"51 -> \"0110011\""
	(cl-format nil "~7,'0',B" decimal))

(defn- trim-str [bitstring]
	(str/trim (str/replace bitstring " " "")))

(defn decode-binary [bitstring]
	"\"0110011\" -> 51"
	(->> (partition 7 (trim-str bitstring))
		 (map #(Integer/parseInt (apply str %) 2))
		 (first)))

(defn classify-scheduled-day
	"bit 연산자를 통해 스케쥴 사용 가능 요일인지 판별"
	[bitstring]
	(let [day-of-week (-> (t/now)
						  (t/to-time-zone (t/time-zone-for-id "Asia/Seoul"))
						  (t/day-of-week))]
		(-> bitstring
			(subs (- day-of-week 1) day-of-week)
			(Integer/parseInt)
			(= 1))))

(defn get-string-byte-length
	"String -> byte array -> get length of byte array"
	[str]
	(alength (.getBytes str)))

(defn json-to-map
	"Json -> Map"
	[json]
	(if (some? json)
		(read-json json)))

(defn map-to-json
	"Map -> JSON"
	[map-data]
	(if (not (empty? map-data))
		(write-str map-data)))

(defn update-map
	"function for all value in map"
	[coll func]
	(reduce-kv (fn [m k v] (assoc m k (func v))) (empty coll) coll))

(defn fix-2-float-point
	"function to help fixed floating point"
	[float]
	(-> (format "%.2f" float)
		(read-string)))