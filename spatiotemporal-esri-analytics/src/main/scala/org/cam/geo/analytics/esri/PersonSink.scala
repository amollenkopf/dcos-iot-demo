package org.cam.geo.analytics.esri
import org.apache.http._
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import java.util.ArrayList
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity

/**
  *  http://services1.arcgis.com/1YRV70GwTj9GYxWK/ArcGIS/rest/services/taxipickupstop/FeatureServer/0/updateFeatures
  *  features: [{ "attributes": { "status": "0", "FID": 1 } }]
  */
object PersonSink {
  val url = "http://services1.arcgis.com/1YRV70GwTj9GYxWK/ArcGIS/rest/services/taxipersonpickup/FeatureServer/0"
  var inCar = false

  def sink(status: String): String = {
    var response = ""
    if (status.equals("1"))
      response = post(status)
    else if (status.equals("2") && !inCar) {
      response = post(status)
      inCar = true
    }
    response
  }

  def post(status: String): String = {
    val post = new HttpPost(url + "/updateFeatures")
    val nameValuePairs = new ArrayList[NameValuePair]()
    nameValuePairs.add(new BasicNameValuePair("features", "[{ \"attributes\": { \"status\": \"" + status + "\", \"FID\": 1 } }]"))
    nameValuePairs.add(new BasicNameValuePair("rollbackOnFailure", "false"))
    nameValuePairs.add(new BasicNameValuePair("f", "pjson"))
    nameValuePairs.add(new BasicNameValuePair("token", ""))
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs))
    val client = new DefaultHttpClient
    val response = client.execute(post).getStatusLine
    response.getStatusCode + ": " + response.getReasonPhrase
  }

}