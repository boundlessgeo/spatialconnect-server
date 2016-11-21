(ns spacon.components.mqtt
  (:require [com.stuartsierra.component :as component]
            [spacon.util.protobuf :as pbf]
            [clojurewerkz.machine-head.client :as mh]
            [clojure.data.json :as json]))

(def id "spacon-server")

(def broker-url (str "tcp://localhost:1883"))

(defn connectmqtt []
  (mh/connect broker-url id))

(defn subscribe [mqtt topic f]
  (mh/subscribe (:conn mqtt) {topic 2} (fn [_ _ ^bytes payload]
                                         (f (String. payload "UTF-8")))))

(defn listenOnTopic [mqtt topic f]
  (subscribe mqtt topic f))

(defn publishMapToTopic [mqtt topic message]
  (let [m {:payload (json/write-str message)}
        p (pbf/map->protobuf m)]
    (mh/publish (:conn mqtt) topic
                (.toByteArray p))))

(defn publishToTopic [mqtt topic message]
      (mh/publish (:conn mqtt) topic (.toByteArray message)))

(defrecord MqttComponent [mqtt-config]
  component/Lifecycle
  (start [this]
    (let [m (connectmqtt)]
      (assoc this :conn m)))

  (stop [this]
    (println "Disconnecting MQTT Client")
    (mh/disconnect (:conn this))
    this))

(defn make-mqtt-component [mqtt-config]
  (map->MqttComponent {:mqtt-config mqtt-config}))