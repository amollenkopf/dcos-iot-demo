package org.cam.geo.analytics.esri

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}
import org.cam.geo.analytics.AnalyticLog
import com.esri.core.geometry.{GeometryEngine, Point, SpatialReference}
import org.codehaus.jackson.JsonFactory
import org.elasticsearch.spark._

object SpatiotemporalEsriAnalyticTaskWithElasticsearchSink {
  //TODO: Get Analytic to work with 2.11 not 2.10, https://hub.docker.com/r/javidelgadillo/mesosphere-spark_2.11/
  //TODO: refactor geofence ring to common AnalyticData object or file
  val geofenceRing =
    "{\"rings\":[[[-117.16371744999998,33.52359333600003],[-117.15562728199995,33.529304042000035],[-117.14967862999998,33.533587072000046],[-117.14325408499997,33.54191518500005],[-117.13540186399996,33.53929777800005],[-117.12350455899997,33.541201347000026],[-117.12041125899998,33.55405043700006],[-117.08376755999996,33.55428838300003],[-117.08400550599998,33.54048750900006],[-117.10089967899995,33.54024956300003],[-117.10018584099998,33.527876366000044],[-117.09471307999996,33.51978619800008],[-117.08900237399996,33.51026835400006],[-117.08091220699998,33.512885761000064],[-117.07091846999998,33.49385007300003],[-117.08733675099995,33.48385633700008],[-117.10518270899996,33.47885946900004],[-117.11636617599999,33.47576616900005],[-117.12493223499996,33.47671795400004],[-117.13016704899997,33.475528223000026],[-117.14111256999996,33.48100098400005],[-117.14539559999997,33.493136235000065],[-117.15086835999995,33.499560780000024],[-117.15753085099999,33.51240986900007],[-117.16371744999998,33.52359333600003]]],\"spatialReference\": {\"wkid\":4326}}"
  val geofence = GeometryEngine.jsonToGeometry(new JsonFactory().createJsonParser(geofenceRing)).getGeometry

  def main(args: Array[String]) {
    if (args.length < 5) {
      System.err.println("Usage: EsriSpatiotemporalAnalyticTask <brokerUrl(s)> <topic(s)> <consumerGroupId> <geofenceFilteringOn>")
      System.err.println("         brokerUrl(s): a comma separated list of Kafka broker urls, e.g. localhost:9092")
      System.err.println("             topic(s): a comma separated list of the Kafka topic(s) name to consume from, e.g. source01")
      System.err.println("      consumerGroupId: the Kafka consumer group id to consume with, e.g. source01-consumer-id")
      System.err.println("  geofenceFilteringOn: indicates whether or not to apply a geofence filter, e.g. true")
      System.err.println("             stdoutOn: indicates whether or not to write to stdout, e.g. true")
      System.exit(1)
    }
    AnalyticLog.setStreamingLogLevels()

    val Array(zkQuorum, topics, consumerGroupId, geofenceFilteringOnStr, stdoutOnStr) = args
    val geofenceFilteringOn = geofenceFilteringOnStr.toBoolean
    val stdoutOn = stdoutOnStr.toBoolean

    val sparkConf = new SparkConf()
      .setAppName("EsriSpatiotemporalAnalyticTask")
      .set("es.cluster.name", "spatiotemporal-store")
      .set("es.nodes", "localhost:9200")
      .set("es.index.auto.create", "true")

    val ssc = new StreamingContext(sparkConf, Seconds(1))
    //ssc.checkpoint("checkpoint")  //note: Use only if HDFS is available where Spark is running

    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String](
      "metadata.broker.list" -> zkQuorum,
      "group.id" -> consumerGroupId
    )

    val lines = KafkaUtils.createDirectStream
      [String, String, StringDecoder, StringDecoder](
        ssc, kafkaParams, topicsSet
      ).map(_._2)

    val filtered = if (geofenceFilteringOn) filterGeofences(lines) else lines

    val datasource = filtered.map(
      // SUSPECT,TRACK_DATE,SENSOR,BATTERY_LEVEL,LATITUDE,LONGITUDE,DISTANCE_FT,DURATION_MIN,SPEED_MPH,COURSE_DEGREE
      // J7890,TIME,2,High,32.97903,-115.550378,78.63,0.87,1.03,123
      line => {  //TODO: move to function mapToDatasource
        val fields = line.split(",")
        val point = (fields(4).toDouble, fields(5).toDouble)
        Map(
          "suspectId" -> fields(0),
          "observationTime" -> fields(1).toLong,
          "sensor" -> fields(2).toInt,
          "batteryLevel" -> fields(3),
          "latitude" -> point._1,
          "longitude" -> point._2,
          "distanceInFeet" -> fields(6).toFloat,
          "durationInMinutes" -> fields(7).toFloat,
          "speedMph" -> fields(8).toFloat,
          "courseDegree" -> fields(9).toFloat,
          "geometry" -> s"${point._1},${point._2}"
        )
      }
    )

    val esIndexName = "test1"
    datasource.foreachRDD((rdd: RDD[Map[String, Any]], time: Time) => {
      rdd.saveToEs(esIndexName + "/" + esIndexName) // ES index/type
      if (stdoutOn) {
        println("--------------------------------------------------------------------------")
        println("Time %s: Elasticsearch sink (saved %s total records to %s)".format(time, rdd.count(), esIndexName))
        println("--------------------------------------------------------------------------")
        for (ds <- rdd)
          println(ds)
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }

  def filterGeofences(lines: DStream[String]): DStream[String] = {
    lines.filter(
      line => {
        val elems = line.split(",")
        val point = new Point(elems(5).toDouble, elems(4).toDouble)
        !GeometryEngine.disjoint(point, geofence, SpatialReference.create(4326))
      })
  }
}
