package org.cam.geo.analytics.esri

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}
import com.esri.core.geometry.{GeometryEngine, Point, SpatialReference}
import org.codehaus.jackson.JsonFactory

// spatiotemporal-event-source$ java -Xms4096m -Xmx4096m -jar target/scala-2.11/spatiotemporal-event-source-assembly-1.0.jar localhost:9092 taxi-pickup 1000 ./data/taxi/taxi-pickup-simulation.csv 1 false
// spatiotemporal-esri-analytics$ $SPARK_HOME/bin/spark-submit --class "org.cam.geo.analytics.esri.SpatiotemporalEsriAnalyticTaskWithTShirtSink" --master local[2] target/scala-2.10/spatiotemporal-esri-analytic-task-assembly-1.0.jar localhost:2181 taxi-pickup true http://esri06agents.westus.cloudapp.azure.com:10003/api/tshirt true

object SpatiotemporalEsriAnalyticTaskWithTShirtSink {
  //TODO: refactor geofence ring to common AnalyticData object or file
  //val geofenceRing =
  //  "{\"rings\":[[[-117.16371744999998,33.52359333600003],[-117.15562728199995,33.529304042000035],[-117.14967862999998,33.533587072000046],[-117.14325408499997,33.54191518500005],[-117.13540186399996,33.53929777800005],[-117.12350455899997,33.541201347000026],[-117.12041125899998,33.55405043700006],[-117.08376755999996,33.55428838300003],[-117.08400550599998,33.54048750900006],[-117.10089967899995,33.54024956300003],[-117.10018584099998,33.527876366000044],[-117.09471307999996,33.51978619800008],[-117.08900237399996,33.51026835400006],[-117.08091220699998,33.512885761000064],[-117.07091846999998,33.49385007300003],[-117.08733675099995,33.48385633700008],[-117.10518270899996,33.47885946900004],[-117.11636617599999,33.47576616900005],[-117.12493223499996,33.47671795400004],[-117.13016704899997,33.475528223000026],[-117.14111256999996,33.48100098400005],[-117.14539559999997,33.493136235000065],[-117.15086835999995,33.499560780000024],[-117.15753085099999,33.51240986900007],[-117.16371744999998,33.52359333600003]]],\"spatialReference\": {\"wkid\":4326}}"
  //val geofenceRing =
  //  "{\"rings\":[[[18,18],[18,22],[22,22],[22,18],[18,18]]],\"spatialReference\": {\"wkid\":4326}}"
  val geofenceRing = // New York Hilton Midtown, 1335 Avenue of the Americas, New York, NY 10019, -73.976776356,40.7664102210001
    "{\"rings\":[[[-73.97598128,40.767997972],[-73.976000372,40.7673929470001],[-73.974818894,40.766388212],[-73.974783814,40.7663726220001],[-73.974246874,40.765895399],[-73.974205216,40.7658763810001],[-73.9725380259999,40.7641346780001],[-73.9720323499999,40.763864547],[-73.9724957509999,40.763222259],[-73.972139958,40.763013308],[-73.970961126,40.7618019550001],[-73.9708875219999,40.761669485],[-73.970819008,40.7616401610001],[-73.970784822,40.7616202720001],[-73.9707848039999,40.7616202340001],[-73.970784793,40.761620205],[-73.970784784,40.7616201760001],[-73.9707847769999,40.761620146],[-73.9707847719999,40.7616201160001],[-73.970784769,40.7616200850001],[-73.970784768,40.761620054],[-73.970784769,40.7616200240001],[-73.9707847719999,40.7616199930001],[-73.9707847769999,40.761619963],[-73.970784784,40.7616199330001],[-73.970784793,40.761619904],[-73.9707848039999,40.7616198750001],[-73.9707848159999,40.7616198470001],[-73.9707848309999,40.7616198200001],[-73.9707848469999,40.7616197940001],[-73.970784865,40.7616197680001],[-73.9709068129999,40.7615943050001],[-73.972291524,40.7597287020001],[-73.97227936,40.759683911],[-73.9722943369999,40.759673046],[-73.9722966319999,40.7597266070001],[-73.9729716639999,40.7599291390001],[-73.975993602,40.7601749580001],[-73.9811858319999,40.761864302],[-73.9829739529999,40.76149731],[-73.983260468,40.761141654],[-73.983260606,40.7611416770001],[-73.983260747,40.7611417100001],[-73.9832608849999,40.7611417520001],[-73.983261021,40.761141803],[-73.983261152,40.761141863],[-73.98326128,40.7611419310001],[-73.983261403,40.761142007],[-73.983261521,40.7611420910001],[-73.9832616319999,40.7611421830001],[-73.983261738,40.761142282],[-73.983261837,40.7611423880001],[-73.983261929,40.7611425000001],[-73.9832620129999,40.7611426170001],[-73.98326209,40.76114274],[-73.9832621579999,40.761142868],[-73.9832622179999,40.7611430000001],[-73.983262269,40.7611431350001],[-73.983262311,40.7611432740001],[-73.983262343,40.761143415],[-73.9832623519999,40.76114346],[-73.9834053609999,40.761962318],[-73.983388351,40.7624522300001],[-73.9833343129999,40.7628353800001],[-73.983300315,40.762892729],[-73.9836696519999,40.7637158480001],[-73.983795087,40.7652699130001],[-73.983814947,40.765410536],[-73.983881809,40.7654393540001],[-73.983915903,40.7654595180001],[-73.9837585079999,40.765520528],[-73.9836314739999,40.767029307],[-73.983134305,40.7668446810001],[-73.982806022,40.766918837],[-73.982764085,40.766931003],[-73.98249139,40.7672868390001],[-73.980885965,40.767639789],[-73.979980626,40.7673151660001],[-73.9776090359999,40.7674866500001],[-73.9775713459999,40.76747084],[-73.9763168709999,40.7676525110001],[-73.9759856749999,40.767999665],[-73.9759856379999,40.767999667],[-73.975985276,40.7679996790001],[-73.975984915,40.767999667],[-73.975984554,40.7679996320001],[-73.9759841969999,40.7679995730001],[-73.975983844,40.7679994910001],[-73.9759834979999,40.7679993850001],[-73.975983159,40.767999258],[-73.975982829,40.767999108],[-73.9759825099999,40.7679989380001],[-73.9759822029999,40.767998747],[-73.9759819079999,40.7679985360001],[-73.9759816279999,40.767998306],[-73.9759813639999,40.767998059],[-73.97598128,40.767997972]]],\"spatialReference\": {\"wkid\":4326}}"


  // -73.9627709009999,40.7667244260001  Car Start
  // -73.976776356,40.7664102210001      Pickup
  // -73.9798789669999,40.752608546      Dropoff

  val geofence = GeometryEngine.jsonToGeometry(new JsonFactory().createJsonParser(geofenceRing)).getGeometry

  def main(args: Array[String]) {
    if (args.length < 5) {
      System.err.println("Usage: EsriSpatiotemporalAnalyticTask <zkQuorum> <topic> <geofenceFilteringOn> <verbose>")
      System.err.println("          zkQuorum(s): the zookeeper url, e.g. localhost:2181")
      System.err.println("                topic: the Kafka topic name to consume from, e.g. taxi03")
      System.err.println("  geofenceFilteringOn: indicates whether or not to apply a geofence filter, e.g. false")
      System.err.println("                  url: the url to invoke, e.g. ")
      System.err.println("              verbose: indicates whether or not to write details to stdout, e.g. false")
      System.exit(1)
    }

    val Array(zkQuorum, topic, geofenceFilteringOnStr, url, verboseStr) = args
    val geofenceFilteringOn = geofenceFilteringOnStr.toBoolean
    val verboseOn = verboseStr.toBoolean


    val sparkConf = new SparkConf()
      .setAppName("rat2")

    val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sc, Seconds(1))
    //ssc.checkpoint("checkpoint")  //note: Use only if HDFS is available where Spark is running

    val numThreads = "1"
    val topicMap = topic.split(",").map((_, numThreads.toInt)).toMap
    val consumerGroupId = topic + "-consumer-group"

    val lines = KafkaUtils.createStream(ssc, zkQuorum, consumerGroupId, topicMap).map(_._2)
    val filtered = if (geofenceFilteringOn) filterGeofences(lines) else lines

    filtered.foreachRDD((rdd: RDD[String], time: Time) => {
      println("Time %s: Spatiotemporal Esri Analytics w/ t-shirt sink (%s total records)".format(time, rdd.count))
      rdd.collect.foreach( line => {
        val response = TShirtSink.sink(url, line)
        if (!response.equals(""))
          println("===== Time %s: TShirt Sink (%s) =====".format(System.currentTimeMillis, response))
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
