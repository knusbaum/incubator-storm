(ns backtype.storm.ui.timelineServer
  (:import [org.apache.hadoop.yarn.client.api TimelineClient])
  (:import [org.apache.hadoop.yarn.api  ApplicationClientProtocol])
  (:import [org.apache.hadoop.yarn.api.records.timeline TimelineEntity TimelineEvent])
  (:import [org.apache.hadoop.yarn.api.protocolrecords GetNewApplicationRequest])
  (:import [org.apache.hadoop.yarn.conf YarnConfiguration])
  (:import [org.apache.hadoop.yarn.ipc YarnRPC])
  (:import [org.apache.hadoop.yarn.util Records])
  (:import [org.apache.hadoop.conf Configuration])
  (:import [org.apache.hadoop.net NetUtils])
  (:use [backtype.storm util log])
  (:require [clj-http.client :as client]))

(def TIMELINE-TOPOLOGY-DIRECTORY "topologies")

(defn- get-and-deserialize 
  [url]
  (from-json (:body (client/get url))))

(defn- send-to-timeline 
  "Send some stuff to the timeline server."
  [timeline-client app-id entity-type entity-id event-type event-data]
  (let [timeline-event (doto (TimelineEvent.)
                         (.setEventType event-type)
                         (.addEventInfo event-data)
                         (.setTimestamp (System/currentTimeMillis)))
        timeline-entity (doto (TimelineEntity.)
                          (.setEntityId entity-id)
                          (.setEntityType entity-type)
                          (.setStartTime (System/currentTimeMillis))
                          (.addEvent timeline-event))
        timeline-response (.putEntities timeline-client 
                                        (into-array TimelineEntity [timeline-entity]))]
    (log-debug "Sent Timeline Data. Response: " timeline-response " | " (.getErrors timeline-response))))


(defn send-topology-timeline-data 
  "Send a bit of topology data to the timeline server. 
   Includes an entry in the topology directory so the UI can find it."
  [app-id entity-type entity-id event-type event-data]
  (let [timeline-client (doto (TimelineClient/createTimelineClient)
                          (.init (Configuration.))
                          (.start))]
    (send-to-timeline timeline-client app-id entity-type entity-id event-type event-data)
    (send-to-timeline timeline-client app-id TIMELINE-TOPOLOGY-DIRECTORY (.toString (uuid)) 
                      "received-event" {:topology-id entity-type})
    (.stop timeline-client)))

(defn get-topology-list 
  "Get a list of the topologies available on the timeline server"
  []
  (let [topology-entities (get 
                           (get-and-deserialize "http://localhost:8188/ws/v1/timeline/topologies") 
                           "entities")]
    (to-json 
     {"topology"
      (flatten (for [entity topology-entities]
                 (for [event (get entity "events")]
                   {"topologyId" (get (get event "eventinfo") ":topology-id")})))})))

(defn get-topology-data
  "Get all events associated with a topology."
  [topology-id]
  (let [topology-entities (get
                           (get-and-deserialize (str "http://localhost:8188/ws/v1/timeline/" topology-id))
                           "entities")]
    (to-json (flatten (for [entity topology-entities]
                        (get entity "events"))))))
       
