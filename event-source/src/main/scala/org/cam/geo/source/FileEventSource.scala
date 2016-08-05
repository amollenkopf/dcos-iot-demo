package org.cam.geo.source

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, Producer, ProducerRecord}
import scala.io.Source

object FileEventSource extends App {
  if (args.length < 6) {
    System.err.println("Usage: org.cam.geo.source.FileEventSource <brokerUrl(s)> <topic> <eventsPerSecond> <intervalInMillis> <filePathAndName> <verbose>")
    System.err.println("      brokerUrl(s): a comma separated list of Kafka broker urls, e.g. localhost:9092")
    System.err.println("             topic: the Kafka topic name to produce to, e.g. source01")
    System.err.println("   eventsPerSecond: the number of events to produce every second, e.g. 4")
    System.err.println("  intervalInMillis: the interval of milliseconds to pause between sending events, e.g. 1000")
    System.err.println("   filePathAndName: the relative or full path of the simulation file, e.g. /data/parolee/parolee.csv")
    System.err.println("           verbose: when true, prints out messages sent to stdout")
    System.exit(1)
  }
  val Array(brokers, topic, eventsPerSecondStr, intervalInMillisStr, fileIn, verboseStr) = args
  val eventsPerSecond = eventsPerSecondStr.toInt
  val intervalInMillis = intervalInMillisStr.toInt
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

  var ix = 0
  var count = 0
  println("Writing " + eventsPerSecond + " e/s every " + intervalInMillis + " millis to topic " + topic + " on brokers " + brokers + " ...")
  while(true) {
    if (ix + eventsPerSecond > tracks.length)
      ix = 0
    for (jx <- ix until ix+eventsPerSecond) {
      val eventString = tracks(jx).replace("TIME", System.currentTimeMillis().toString).trim
      producer.send(new ProducerRecord[String, String](topic, eventString.split(",")(0), eventString))
      ix += 1
      count += 1
      if (verbose)
        println(jx + ": " + eventString)
    }
    if (verbose)
      println()
    println("Time %s: File Event Source sent %s events.".format(System.currentTimeMillis, count))
    count = 0
    Thread.sleep(intervalInMillis)
  }
  producer.close()
}
