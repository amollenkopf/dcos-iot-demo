package controllers

import javax.inject.Inject

import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import org.apache.http.client.methods.{CloseableHttpResponse, HttpRequestBase}
import play.api.http.{ContentTypes, HeaderNames, HttpEntity}
import play.api.mvc.{Action, Controller, Request, Result}
import utils.{ConfigUtils, HttpClientService}

class ProxyController @Inject() extends Controller {
  private val targetHost = ConfigUtils.internalHost
  private val targetPort = ConfigUtils.internalPort

  private val ignoreHeaders = Seq(HeaderNames.CONTENT_LENGTH)

  private def copyHeaders(httpRequest: Request[Any], req: HttpRequestBase, appId: String, port: Int) = {
    httpRequest.headers.toMap.foreach(entry => {
      if (!ignoreHeaders.exists(ih => ih.equals(entry._1)))
        entry._2.foreach(value => {
          req.removeHeaders(entry._1)
          req.addHeader(entry._1, value)
        })
    })

    req.addHeader(HeaderNames.X_FORWARDED_FOR, httpRequest.remoteAddress)
    //req.addHeader(HeaderNames.HOST, s"$targetHost:$targetPort")
  }


  private val responseHeadersToIgnore = Seq(HeaderNames.CONTENT_TYPE, HeaderNames.CONTENT_LENGTH)

  private def convertToResponse(response: CloseableHttpResponse, appId: String): Result = {
    try {
      val result = Status(response.getStatusLine.getStatusCode)
      println(s"status: ${response.getStatusLine.getStatusCode}")
      val contentType = response.getFirstHeader(CONTENT_TYPE)
      val returnResult = {
        val replyEntity = response.getEntity
        val entity = if (replyEntity != null) {
          // the trinity way
          val source: Source[ByteString, _] = StreamConverters.fromInputStream(response.getEntity.getContent)
          HttpEntity.Streamed(source, None, Some(contentType.getValue))

          // the new proxy way - might limit the returned content size.
          //HttpEntity.Strict(ByteString(EntityUtils.toString(replyEntity)), Option(replyEntity.getContentType.getValue))
        } else {
          HttpEntity.NoEntity
        }
        result.copy(body = entity)
      }
      var finalResult = returnResult.copy()
      response.getAllHeaders.filter(hdr => !responseHeadersToIgnore.contains(hdr.getName)).foreach(header => {
        finalResult = finalResult.withHeaders(header.getName -> header.getValue)
        println(s"${header.getName}: ${header.getValue}")
      }
      )
      finalResult
    } catch {
      case t: Throwable =>
        t.printStackTrace()
        InternalServerError
    }
  }

  private def addRawQueryIfNecessary(url: String, rawQuery: String): String = {
    rawQuery match {
      case empty if empty.isEmpty => url
      case _ => s"$url?$rawQuery"
    }
  }

  private def convertRequestToInternalUrl(appId: String, path: String, rawQueryString: String, preferSecure: Boolean): (String, Int) = {
    val pathToAppend = if (Option(path).getOrElse("").nonEmpty) s"$path" else ""
    val url = s"http://$targetHost:$targetPort/$appId/$pathToAppend"

    println(s"internal url: $url:$targetPort")
    (addRawQueryIfNecessary(url, rawQueryString), targetPort)
  }

  def proxyDelete(appId: String, path: String) = Action { httpRequest =>
    convertRequestToInternalUrl(appId, path, httpRequest.rawQueryString, httpRequest.secure) match {
      case ("", _) => NotFound
      case (internalUrl: String, port: Int) =>
        val req = HttpClientService.createNewDeleteRequest(internalUrl)
        copyHeaders(httpRequest, req, appId, port)
        val client = HttpClientService.createNewClient()
        val response = client.execute(req)
        convertToResponse(response, appId)
    }
  }

  def proxyPut(appId: String, path: String) = Action(parse.tolerantText) { httpRequest =>
    convertRequestToInternalUrl(appId, path, httpRequest.rawQueryString, httpRequest.secure) match {
      case ("", _) => NotFound
      case (internalUrl: String, port: Int) =>
        httpRequest.contentType match {
          case None => BadRequest("Missing Content-Type").as(ContentTypes.TEXT)
          case Some(contentType) =>
            val req = HttpClientService.createNewPutRequest(internalUrl, httpRequest.body, contentType)
            copyHeaders(httpRequest, req, appId, port)
            val client = HttpClientService.createNewClient()
            val response = client.execute(req)
            convertToResponse(response, appId)
        }
    }
  }

  def proxyPost(appId: String, path: String) = Action(parse.tolerantText) { httpRequest =>
    convertRequestToInternalUrl(appId, path, httpRequest.rawQueryString, httpRequest.secure) match {
      case ("", _) => NotFound
      case (internalUrl: String, port: Int) =>
        httpRequest.contentType match {
          case None => BadRequest("Missing Content-Type").as(ContentTypes.TEXT)
          case Some(contentType) =>
            val req = HttpClientService.createNewPostRequest(internalUrl, httpRequest.body, contentType)
            copyHeaders(httpRequest, req, appId, port)
            val client = HttpClientService.createNewClient()
            val response = client.execute(req)
            convertToResponse(response, appId)
        }
    }
  }

  def proxyGet(appId: String, path: String) = Action { httpRequest =>
    convertRequestToInternalUrl(appId, path, httpRequest.rawQueryString, httpRequest.secure) match {
      case ("", _) => NotFound
      case (internalUrl: String, port: Int) =>
        val req = HttpClientService.createNewGetRequest(internalUrl)
        copyHeaders(httpRequest, req, appId, port)
        val client = HttpClientService.createNewClient()
        val response = client.execute(req)
        convertToResponse(response, appId)
    }
  }
}
