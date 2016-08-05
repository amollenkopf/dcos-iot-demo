package org.cam.geo.analytics.esri

import java.io.IOException
import java.net.SocketTimeoutException

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.{BasicHttpParams, HttpConnectionParams}

object TShirtSink extends App {
  var alerted = false

  def sink(url: String, message: String): String = {
    var response = ""
    if (!alerted) {
      alerted = true
      try {
        response = post(url, message)
      } catch {
        case ste: SocketTimeoutException => response = "SocketTimeout"
        case ioe: IOException => response = "IOException"
      }
    }
    response
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  def post(url: String, message: String): String = {
    val httpParams = new BasicHttpParams()
    HttpConnectionParams.setConnectionTimeout(httpParams, 5000)
    HttpConnectionParams.setSoTimeout(httpParams, 5000)
    val client = new DefaultHttpClient(httpParams)

    val post = new HttpPost(url)
    post.setHeader("Content-Type", "application/json")
    val body = "{ \"message\":\"" + message + "\", \"rgb\":[255,255,255], \"animation\":3000 }"
    val entity = new StringEntity(body)
    println(body)
    post.setEntity(entity)

    /*
    {
      "message":"My message",
      "rgb":[255,255,255],
      "animation":3000
    }
    */

    //val client = new DefaultHttpClient
    val response = client.execute(post).getStatusLine
    println(response)
    response.getStatusCode + ": " + response.getReasonPhrase
  }
  //TODO: To resolve deprecations, see http://stackoverflow.com/questions/3000214/java-http-client-request-with-defined-timeout

  //  bad: http://clcamesos25agents.westus.cloudapp.azure.com:10001/api/tshirt
  // good: http://clcamesos25agents.westus.cloudapp.azure.com:10001/api/tshirtfake

  //println(sink("http://clcamesos25agents.westus.cloudapp.azure.com:10001/api/tshirt", "Adam Test 1"))
  val message: String = args(0).toString
  println("sending " + message)
  println(sink("http://esri12agents.westus.cloudapp.azure.com:10005/api/tshirt", message))
}