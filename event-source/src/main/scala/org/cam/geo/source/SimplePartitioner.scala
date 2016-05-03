package org.cam.geo.source

import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.common.Cluster

class SimplePartitioner extends Partitioner {

  def configure(configs: java.util.Map[String, _]): Unit = { }

  def partition(topic: String, key: Any, keyBytes: Array[Byte], value: Any, valueBytes: Array[Byte], cluster: Cluster): Int = {
    println(
      Math.abs(key.hashCode()) + " % " + cluster.availablePartitionsForTopic(topic).size + " = " +
      Math.abs(key.hashCode()) % cluster.availablePartitionsForTopic(topic).size)
    Math.abs(key.hashCode()) % cluster.availablePartitionsForTopic(topic).size
  }

  def close(): Unit = { }
}
