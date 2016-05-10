package org.cam.geo.source

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, Producer, ProducerRecord}
import scala.io.Source

/*
 *  Integrate and test NYC airport polygon filter
 *  Add Spark code snippets to PPT
 *  Make demo videos: Taxis + Filtering + DC/OS exploration
 *  Update doc on GitHub and DockerHub
 */

object SpatiotemporalFileEventSource extends App {
  if (args.length < 6) {
    System.err.println("Usage: org.cam.geo.source.SpatiotemporalFileEventSource <brokerUrl(s)> <topic> <intervalInMillis> <filePathAndName> <timeFieldIx> <verbose>")
    System.err.println("      brokerUrl(s): a comma separated list of Kafka broker urls, e.g. localhost:9092")
    System.err.println("             topic: the Kafka topic name to produce to, e.g. source01")
    System.err.println("  intervalInMillis: the interval of milliseconds to pause between sending events, e.g. 1000")
    System.err.println("   filePathAndName: the relative or full path of the simulation file, e.g. /data/parolee/parolee.csv")
    System.err.println("       timeFieldIx: the field index of the simulation timestamp field, e.g. 1")
    System.err.println("           verbose: when true, prints out messages sent to stdout")
    System.exit(1)
  }
  val Array(brokers, topic, intervalInMillisStr, fileIn, timeFieldIxStr, verboseStr) = args
  val intervalInMillis = intervalInMillisStr.toInt
  val timeFieldIx = timeFieldIxStr.toInt
  val verbose = verboseStr.toBoolean

  val readStart = System.currentTimeMillis()
  println("Reading from " + fileIn + " ...")
  val tracks = Source.fromFile(fileIn).getLines.toArray[String]
  println("Read " + tracks.length + " events in " + (System.currentTimeMillis() - readStart) + " milliseconds.")

  val props: Properties = new Properties
  props.put("bootstrap.servers", brokers)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("partitioner.class", classOf[SimplePartitioner])
  val producer: Producer[String, String] = new KafkaProducer[String, String](props)
  println("Writing to topic " + topic + " on brokers " + brokers + " ...")
  var ix = 0
  var count = 0
  var lastTimestamp = ""
  var timeMillis = System.currentTimeMillis()
  for (line <- tracks) {
    val elems = line.trim.split(",")
    if (lastTimestamp.equals(""))
      lastTimestamp = elems(timeFieldIx)
    if (!lastTimestamp.equals(elems(timeFieldIx))) {
      lastTimestamp = elems(timeFieldIx)
      timeMillis = System.currentTimeMillis()
      println("Sent " + count + " events.")
      count = 0
      Thread.sleep(intervalInMillis)
    }
    val eventString = line.trim.replace(lastTimestamp, timeMillis.toString)
    producer.send(new ProducerRecord[String, String](topic, elems(0), eventString))
    if (verbose)
      println(ix + ": " + eventString)
    count += 1
    ix += 1
  }
  producer.close()
}
