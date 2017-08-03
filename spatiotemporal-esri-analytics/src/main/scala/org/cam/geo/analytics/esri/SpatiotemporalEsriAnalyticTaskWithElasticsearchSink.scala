package org.cam.geo.analytics.esri

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}
import com.esri.core.geometry.{GeometryEngine, Point, SpatialReference}
import org.cam.geo.sink.{ElasticsearchUtils, EsField, EsFieldType}
import org.codehaus.jackson.JsonFactory
import org.elasticsearch.hadoop.cfg.ConfigurationOptions
import org.elasticsearch.spark._

object SpatiotemporalEsriAnalyticTaskWithElasticsearchSink {
  val geofenceRing =
    "{\"rings\":[[[-73.794732995,40.648205273],[-73.794322526,40.648828135],[-73.783855573,40.653239905],[-73.770515339,40.647219064],[-73.77010487,40.645402324],[-73.770378516,40.644052714],[-73.777288073,40.635020004],[-73.780982292,40.634189349],[-73.795553932,40.64073048],[-73.795348698,40.646907626],[-73.794732995,40.648205273]]],\"spatialReference\": {\"wkid\":4326}}"
  val geofence = GeometryEngine.jsonToGeometry(new JsonFactory().createJsonParser(geofenceRing)).getGeometry

  def main(args: Array[String]) {
    if (args.length < 7) {
      System.err.println("Usage: EsriSpatiotemporalAnalyticTask <zkQuorum> <topic> <geofenceFilteringOn> <verbose> <esNode> <esClusterName> <esIndexName>")
      System.err.println("          zkQuorum(s): the zookeeper url, e.g. localhost:2181")
      System.err.println("                topic: the Kafka topic name to consume from, e.g. taxi03")
      System.err.println("  geofenceFilteringOn: indicates whether or not to apply a geofence filter, e.g. false")
      System.err.println("              verbose: indicates whether or not to write details to stdout, e.g. false")
      System.err.println("               esNode: the hostname and port of the Elasticsearch cluster, e.g. localhost:9200")
      System.err.println("        esClusterName: the name of the Elasticsearch cluster, e.g. spatiotemporal-store")
      System.err.println("          esIndexName: the name of the Elasticsearch index to write to, e.g. taxi03")
      System.err.println("           esUserName: (Optional) the username to authenticate with the Elasticsearch cluster, e.g. elastic")
      System.err.println("           esPassword: (Optional) the password to authenticate with the Elasticsearch cluster, e.g. changeme")
      System.exit(1)
    }
    // check for the optional username and password
    val (esUserName:Option[String], esPassword:Option[String]) = {
      if (args.length >= 9) {
        (Option(args(7)), Option(args(8)))
      } else {
        (None, None)
      }
    }

    val Array(zkQuorum, topic, geofenceFilteringOnStr, verboseStr, esNodes, esClusterName, esIndexName) = args
    val geofenceFilteringOn = geofenceFilteringOnStr.toBoolean
    val verbose = verboseStr.toBoolean
    val shards = 20  //TODO: expose as param, default is 3
    val replicas = 0  //TODO: expose as param, default is 1

    val sparkConf = new SparkConf()
      .setAppName("taxi-rat")
      .set("es.cluster.name", esClusterName)
      .set("es.nodes", esNodes)
      .set("es.index.auto.create", "true")

    // add the proper authentication credentials to the ES Spark connector
    if (esUserName.exists(str => str != null && str.nonEmpty) && esPassword.exists(str => str != null && str.nonEmpty) ) {
      sparkConf.set(ConfigurationOptions.ES_NET_HTTP_AUTH_USER, esUserName.orNull)
      sparkConf.set(ConfigurationOptions.ES_NET_HTTP_AUTH_PASS, esPassword.orNull)
    }

    val ssc = new StreamingContext(sparkConf, Seconds(1))
    //ssc.checkpoint("checkpoint")  //note: Use only if HDFS is available where Spark is running

    val numThreads = "1"
    val topicMap = topic.split(",").map((_, numThreads.toInt)).toMap
    val consumerGroupId = topic + "-consumer-group"

    val lines = KafkaUtils.createStream(ssc, zkQuorum, consumerGroupId, topicMap).map(_._2)
    val filtered = if (geofenceFilteringOn) filterGeofences(lines) else lines

    val datasource = filtered.map(
      // taxiId, observationTime,     longitude,  latitude,  passengerCount, tripTime, tripDistance, observationIx, medallion,                        hack_license,                     vendor_id, rate_code, store_and_fwd_flag, pickup_datetime,     dropoff_datetime,    pickup_longitude, pickup_latitude, dropoff_longitude, dropoff_latitude
      // 190,    2013-01-25 00:00:00, -73.788591, 40.641935, 5,              1,        14.26,        1,             710920BDB014F222CF591EEBB7D89EE2, 0183FD2DF44DBC4CCAF9A2E721A9C620, VTS,       1,         ,                   2013-01-25 00:00:00, 2013-01-25 00:36:00, -73.78862,        40.641865,       -73.989281,        40.65889
      line => {  //TODO: move to function mapToDatasource
        val fields = line.split(",")
        val point = (fields(3).toDouble, fields(2).toDouble)
        Map(
          "taxiId" -> fields(0),
          "observationTime" -> fields(1).toLong,
          "longitude" -> point._2,
          "latitude" -> point._1,
          "passengerCount" -> fields(4).toInt,
          "tripTimeInSecs" -> fields(5).toFloat,
          "tripDistance" -> fields(6).toFloat,
          "observationIx" -> fields(7).toInt,
          "medallion" -> fields(8),
          "hack_license" -> fields(9),
          "vendor_id" -> fields(10),
          "rate_code" -> fields(11),
          "store_and_fwd_flag" -> fields(12),
          "pickup_datetime" -> fields(13),
          "dropoff_datetime" -> fields(14),
          "pickup_longitude" -> fields(15).toDouble,
          "pickup_latitude" -> fields(16).toDouble,
          "dropoff_longitude" -> fields(17).toDouble,
          "dropoff_latitude" -> fields(18).toDouble,
          "geometry" -> s"${point._1},${point._2}"
        )
      }
    )

    val esNode = esNodes.split(":")(0)
    val esPort = esNodes.split(":")(1).toInt //TODO: Clean up
    val esFields = Array(
        EsField("taxiId", EsFieldType.Keyword),
        EsField("observationTime", EsFieldType.Date),
        EsField("longitude", EsFieldType.Double),
        EsField("latitude", EsFieldType.Double),
        EsField("passengerCount", EsFieldType.Integer),
        EsField("tripTimeInSecs", EsFieldType.Float),
        EsField("tripDistance", EsFieldType.Float),
        EsField("observationIx", EsFieldType.Integer),
        EsField("medallion", EsFieldType.Keyword),
        EsField("hack_license", EsFieldType.Keyword),
        EsField("vendor_id", EsFieldType.Keyword),
        EsField("rate_code", EsFieldType.Keyword),
        EsField("store_and_fwd_flag", EsFieldType.Keyword),
        EsField("pickup_datetime", EsFieldType.Keyword),
        EsField("dropoff_datetime", EsFieldType.Keyword),
        EsField("pickup_longitude", EsFieldType.Double),
        EsField("pickup_latitude", EsFieldType.Double),
        EsField("dropoff_longitude", EsFieldType.Double),
        EsField("dropoff_latitude", EsFieldType.Double),
        EsField("geometry", EsFieldType.GeoPoint)
      )
    if (!ElasticsearchUtils.doesDataSourceExists(esIndexName, esNode, esPort, esUserName, esPassword))
      ElasticsearchUtils.createDataSource(esIndexName, esFields, esNode, esPort, esUserName, esPassword, shards, replicas)

    datasource.foreachRDD((rdd: RDD[Map[String, Any]], time: Time) => {
      rdd.saveToEs(esIndexName + "/" + esIndexName) // ES index/type
      println("Time %s: Elasticsearch sink (saved %s total records to %s)".format(time, rdd.count(), esIndexName))
      if (verbose)
        for (ds <- rdd)
          println(ds)
    })

    ssc.start()
    ssc.awaitTermination()
  }

  def filterGeofences(lines: DStream[String]): DStream[String] = {
    lines.filter(
      line => {
        val elems = line.split(",")
        val point = new Point(elems(2).toDouble, elems(3).toDouble)
        !GeometryEngine.disjoint(point, geofence, SpatialReference.create(4326))
      })
  }
}
