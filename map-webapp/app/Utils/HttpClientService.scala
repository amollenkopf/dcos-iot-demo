package utils

import org.apache.http.client.methods._
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}

object HttpClientService extends Serializable {

  def createNewClient(): CloseableHttpClient = {
    val builder = HttpClients.custom()
    builder.useSystemProperties()
    builder.disableRedirectHandling()
    builder.build()
  }

  def addContentAndPayload(request: HttpEntityEnclosingRequestBase, payload: String, contentTypes: String): HttpEntityEnclosingRequestBase = {
    if (payload != null) {
      val contentType = ContentType.create(contentTypes)
      val entity = new StringEntity(payload, contentType)
      request.setEntity(entity)
    }
    request
  }

  def createNewDeleteRequest(url: String): HttpDelete = {
    val request = new HttpDelete(url)
    request
  }

  def createNewPutRequest(url: String, payload: String, contentTypes: String): HttpPut = {
    val request = new HttpPut(url)
    addContentAndPayload(request, payload, contentTypes).asInstanceOf[HttpPut]
  }

  def createNewPostRequest(url: String, payload: String, contentTypes: String): HttpPost = {
    val request = new HttpPost(url)
    addContentAndPayload(request, payload, contentTypes).asInstanceOf[HttpPost]
  }

  def createNewGetRequest(url: String): HttpGet = {
    val request = new HttpGet(url)
    request
  }


}
