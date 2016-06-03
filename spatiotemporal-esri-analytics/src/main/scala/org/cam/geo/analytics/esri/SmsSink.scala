package org.cam.geo.analytics.esri

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient

/**
  * Created by cory6458 on 5/31/16.
  */
object SmsSink {
  val url = "http://clcamesos25agents.westus.cloudapp.azure.com:10002/api/sms"
  val message = "Your driver is 1 minute away."

  def sink(phoneNumber: String): String = {
    post(phoneNumber)
  }

  def post(phoneNumber: String): String = {
    val post = new HttpPost(url + "/send")
    post.setHeader("Content-Type", "application/json")
    post.setEntity(new StringEntity("{ \"to\": \"" + phoneNumber + "\", \"message\": \"" + message + "\" }"))
    val client = new DefaultHttpClient
    val response = client.execute(post).getStatusLine
    response.getStatusCode + ": " + response.getReasonPhrase
  }
}
