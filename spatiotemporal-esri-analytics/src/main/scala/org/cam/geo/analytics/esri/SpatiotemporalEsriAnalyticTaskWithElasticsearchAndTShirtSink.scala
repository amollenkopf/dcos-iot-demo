package org.cam.geo.analytics.esri

import com.esri.core.geometry.{GeometryEngine, Point, SpatialReference}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}
import org.apache.spark.SparkConf
import org.cam.geo.analytics.AnalyticLog
import org.cam.geo.sink.{ElasticsearchUtils, EsField, EsFieldType}
import org.codehaus.jackson.JsonFactory
import org.elasticsearch.spark._

/*
 *  spatiotemporal-esri-analytics$ $SPARK_HOME/bin/spark-submit --class "org.cam.geo.analytics.esri.SpatiotemporalEsriAnalyticTaskWithElasticsearchAndTShirtSink" --master local[2] target/scala-2.10/spatiotemporal-esri-analytic-task-assembly-1.0.jar localhost:2181 source02 true http://esri06agents.westus.cloudapp.azure.com:10003/api/tshirt "MesosCon 2016" xxxxxxxxxx false localhost:9200 spatiotemporal-store taxi-pickup
 *  spatiotemporal-event-source$ java -Xms4096m -Xmx4096m -jar target/scala-2.11/spatiotemporal-event-source-assembly-1.0.jar localhost:9092 source02 1000 ./data/taxi/taxi-route.csv 1 false
 *
 *  simulation$ python taxiSimulation.py data/taxi-pickup.csv results/taxi-pickup-route.csv 0
 *  simulation$ python taxiSimulation.py data/taxi-dropoff.csv results/taxi-dropoff-route.csv 0
 *  simulation$ cp results/taxi-route.csv ~/iot/dcos-iot-demo/spatiotemporal-event-source/data/taxi
 *
 *  http://services1.arcgis.com/1YRV70GwTj9GYxWK/arcgis/rest/services/geofence/FeatureServer/0
 *
 *  Sense:
 *  GET taxi-pickup/taxi-pickup/_count
 *  GET taxi-pickup/_mapping
 *  GET taxi-pickup/_search?taxiId="0"
 *  DELETE test2
 */

object SpatiotemporalEsriAnalyticTaskWithElasticsearchAndTShirtSink {
  //TODO: refactor geofence ring to common AnalyticData object or file
  //val geofenceRing =
  //  "{\"rings\":[[[18,18],[18,22],[22,22],[22,18],[18,18]]],\"spatialReference\": {\"wkid\":4326}}"
  val geofenceRing = // 1 minute travel time from corner of 57th and 7th: -73.98028135299683,40.76534770942323
    "{\"rings\":[[[-73.980991601,40.7676834000001],[-73.978606316,40.7666898320001],[-73.977756061,40.766578795],[-73.976935919,40.7669050580001],[-73.976567495,40.766454088],[-73.975734339,40.7658772560001],[-73.974559843,40.765031224],[-73.9738630639999,40.76469157],[-73.974375966,40.7640076710001],[-73.9745814409999,40.763642385],[-73.974115135,40.763323293],[-73.9740663709999,40.7633026570001],[-73.9738671119999,40.763547089],[-73.973593619,40.7628462500001],[-73.973037854,40.762576934],[-73.9740866349999,40.7621544080001],[-73.974335695,40.7622303250001],[-73.9743817899999,40.7622161460001],[-73.974525425,40.7621596330001],[-73.974730081,40.7619012480001],[-73.976187185,40.761382751],[-73.97826973,40.760625326],[-73.980356851,40.7598987590001],[-73.982446249,40.759117497],[-73.984518441,40.7583487300001],[-73.985191699,40.7583455660001],[-73.985471312,40.757980887],[-73.985392159,40.7585873890001],[-73.9854077589999,40.7587386930001],[-73.9853813669999,40.758806577],[-73.985771078,40.7596976620001],[-73.986073378,40.761456239],[-73.985860327,40.762431668],[-73.9863199119999,40.763200452],[-73.986587656,40.7649433890001],[-73.986801675,40.7664249740001],[-73.986837964,40.766695025],[-73.986721809,40.7666563720001],[-73.986658463,40.766629972],[-73.986464911,40.7667749990001],[-73.98538796,40.7677814960001],[-73.9837900699999,40.767619429],[-73.983765944,40.767654915],[-73.983469182,40.7677705740001],[-73.981910267,40.767920707],[-73.9818728879999,40.767938013],[-73.981415119,40.7681675050001],[-73.981415016,40.768167513],[-73.981414795,40.7681675200001],[-73.981414574,40.768167513],[-73.9814143529999,40.768167491],[-73.981414135,40.768167455],[-73.981413919,40.768167405],[-73.9814138789999,40.768167394],[-73.981131407,40.767766282],[-73.981062418,40.7677380310001],[-73.980991601,40.7676834000001]]],\"spatialReference\": {\"wkid\":4326}}"
  val geofence = GeometryEngine.jsonToGeometry(new JsonFactory().createJsonParser(geofenceRing)).getGeometry

  def main(args: Array[String]) {
    if (args.length < 10) {
      System.err.println("Usage: SpatiotemporalEsriAnalyticTaskWithElasticsearchAndTShirtSink <zkQuorum> <topic> <geofenceFilteringOn> <verbose>")
      System.err.println("          zkQuorum(s): the zookeeper url, e.g. localhost:2181")
      System.err.println("                topic: the Kafka topic name to consume from, e.g. taxi03")
      System.err.println("  geofenceFilteringOn: indicates whether or not to apply a geofence filter, e.g. false")
      System.err.println("                  url: the TShirt url to invoke, e.g. ")
      System.err.println("        tShirtMessage: the TShirt message to send, e.g. MesosCon 2016")
      System.err.println("          phoneNumber: the url to invoke, e.g. ")
      System.err.println("              verbose: indicates whether or not to write details to stdout, e.g. false")
      System.err.println("               esNode: the hostname and port of the Elasticsearch cluster, e.g. localhost:9200")
      System.err.println("        esClusterName: the name of the Elasticsearch cluster, e.g. spatiotemporal-store")
      System.err.println("          esIndexName: the name of the Elasticsearch index to write to, e.g. taxi03")

      System.exit(1)
    }
    AnalyticLog.setStreamingLogLevels()

    val Array(zkQuorum, topic, geofenceFilteringOnStr, url, tShirtMessage, phoneNumber, verboseStr, esNodes, esClusterName, esIndexName) = args
    val geofenceFilteringOn = geofenceFilteringOnStr.toBoolean
    val verbose = verboseStr.toBoolean

    val sparkConf = new SparkConf()
      .setAppName("rat2")
      .set("es.cluster.name", esClusterName)
      .set("es.nodes", esNodes)
      .set("es.index.auto.create", "true")
      .set("es.write.operation", "upsert")
      .set("es.mapping.id", "taxiId")

    val ssc = new StreamingContext(sparkConf, Seconds(1))
    //ssc.checkpoint("checkpoint")  //note: Use only if HDFS is available where Spark is running

    val numThreads = "1"
    val topicMap = topic.split(",").map((_, numThreads.toInt)).toMap
    val consumerGroupId = topic + "-consumer-group"

    val lines = KafkaUtils.createStream(ssc, zkQuorum, consumerGroupId, topicMap).map(_._2)

    val datasource = lines.map(
      // taxiId, observationTime,     longitude,  latitude,  passengerCount, tripTime, tripDistance, observationIx, medallion,                        hack_license,                     vendor_id, rate_code, store_and_fwd_flag, pickup_datetime,     dropoff_datetime,    pickup_longitude, pickup_latitude, dropoff_longitude, dropoff_latitude
      // 190,    2013-01-25 00:00:00, -73.788591, 40.641935, 5,              1,        14.26,        1,             710920BDB014F222CF591EEBB7D89EE2, 0183FD2DF44DBC4CCAF9A2E721A9C620, VTS,       1,         ,                   2013-01-25 00:00:00, 2013-01-25 00:36:00, -73.78862,        40.641865,       -73.989281,        40.65889
      line => {  //TODO: move to function mapToDatasource
        val fields = line.split(",")
        val point = (fields(3).toDouble, fields(2).toDouble)
        Map(
          "objectid" -> fields(0).toLong,
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
          "geometry" -> s"${point._1},${point._2}",
          "---geo_hash---" -> s"${point._1},${point._2}"
        )
      }
    )

    val esNode = esNodes.split(":")(0)
    val esPort = esNodes.split(":")(1).toInt //TODO: Clean up
    val esFields = Array(
        EsField("objectid", EsFieldType.Long),
        EsField("taxiId", EsFieldType.String),
        EsField("observationTime", EsFieldType.Date),
        EsField("longitude", EsFieldType.Double),
        EsField("latitude", EsFieldType.Double),
        EsField("passengerCount", EsFieldType.Integer),
        EsField("tripTimeInSecs", EsFieldType.Float),
        EsField("tripDistance", EsFieldType.Float),
        EsField("observationIx", EsFieldType.Integer),
        EsField("medallion", EsFieldType.String),
        EsField("hack_license", EsFieldType.String),
        EsField("vendor_id", EsFieldType.String),
        EsField("rate_code", EsFieldType.String),
        EsField("store_and_fwd_flag", EsFieldType.String),
        EsField("pickup_datetime", EsFieldType.String),
        EsField("dropoff_datetime", EsFieldType.String),
        EsField("pickup_longitude", EsFieldType.Double),
        EsField("pickup_latitude", EsFieldType.Double),
        EsField("dropoff_longitude", EsFieldType.Double),
        EsField("dropoff_latitude", EsFieldType.Double),
        EsField("geometry", EsFieldType.GeoPoint)
      )
    if (!ElasticsearchUtils.doesDataSourceExists(esIndexName, esNode, esPort))
      ElasticsearchUtils.createDataSource(esIndexName, esFields, esNode, esPort)

    datasource.foreachRDD((rdd: RDD[Map[String, Any]], time: Time) => {
      println("Time %s: Updating Elasticsearch (%s total records)".format(time, rdd.count))
      rdd.saveToEs(esIndexName + "/" + esIndexName, Map("es.mapping.id" -> "taxiId")) // ES index/type
      if (verbose)
        for (ds <- rdd)
          println(ds)
    })

    val filtered = if (geofenceFilteringOn) filterGeofences(lines) else lines
    filtered.foreachRDD((rdd: RDD[String], time: Time) => {
      rdd.collect.foreach( line => {
        val response = TShirtSink.sink(url, tShirtMessage)
        if (!response.equals("")) {
          println("\n\n====TS==== Time %s: TShirt Sink (%s) ====TS====".format(System.currentTimeMillis, response))
          val personResponse = PersonSink.sink("1")
          println("====PS==== Time %s: Person Sink (%s) to 1 ====PS====\n\n".format(System.currentTimeMillis, personResponse))
          val smsResponse = SmsSink.sink(phoneNumber)
          println("====SS==== Time %s: Sms Sink (%s) ====SS====\n\n".format(System.currentTimeMillis, smsResponse))
        }
        val elems = line.split(",")
        if (elems(2).equals("-73.980201") && elems(3).equals("40.765290")) {
          val personResponse = PersonSink.sink("2")
          if (!personResponse.equals(""))
            println("\n\n====PS==== Time %s: Person Sink to 2 (%s) ====PS====\n\n".format(System.currentTimeMillis, personResponse))
        }
      })
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