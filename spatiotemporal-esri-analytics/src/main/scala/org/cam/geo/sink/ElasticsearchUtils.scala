package org.cam.geo.sink

import java.net.URL

import org.apache.commons.codec.binary.Base64
import org.apache.http.{HttpHeaders, HttpStatus}
import org.apache.http.client.methods.{HttpGet, _}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.json4s._
import org.json4s.jackson.JsonMethods._

object ElasticsearchUtils {

  /**
    *
    * @param dataSourceName the name of the data source
    * @param esHostName the es host name
    * @param esPort the es port
    * @return
    */
  //TODO: Change DataSource references to Index
  def doesDataSourceExists(dataSourceName:String, esHostName:String, esPort:Int = 9200, userName:String, password:String):Boolean = {
    val client = HttpClients.createDefault()
    try {
      val dataSourceNameToLowercase = dataSourceName.toLowerCase()
      val existsURLStr = s"http://$esHostName:$esPort/$dataSourceNameToLowercase"
      val existsURL:URL = new URL(existsURLStr)
      val httpGet:HttpGet = new HttpGet(existsURL.toURI)
      addAuthorizationHeader(httpGet, userName, password)

      val existsResponse = client.execute(httpGet)
      val existsResponseAsString = getHttpResponseAsString(existsResponse, httpGet)
      try {
        val existsOpt = existsResponseAsString.map(str => {
          implicit val formats = DefaultFormats
          (parse(str) \ "error").extractOpt.isEmpty
        })
        existsOpt match {
          case Some(exists) =>
            exists

          case _ =>
            println(s"Failed to determine if DataSource $dataSourceName exist!")
            false
        }
      } catch {
        case parseError: Throwable =>
          println(s"Failed to determine if DataSource $dataSourceName exist!")
          parseError.printStackTrace()
          false
      }
    } finally {
      client.close()
    }
  }

  /**
    * Helper method used to create basic datasource metadata
    *
    * @param dataSourceName the name of the data source
    * @param fields the fields
    * @param esHostName the es host name
    * @param esPort the es port name
    */
  def createDataSource(dataSourceName:String, fields:Array[EsField], esHostName:String, esPort:Int = 9200, userName:String, password:String, shards:Int=3, replicas:Int=1):Boolean = {
    val client = HttpClients.createDefault()
    try {
      val dataSourceNameToLowercase = dataSourceName.toLowerCase()
      val createMappingURLStr = s"http://$esHostName:$esPort/$dataSourceNameToLowercase"
      val createMappingURL:URL = new URL(createMappingURLStr)
      val httpPut:HttpPut = new HttpPut(createMappingURL.toURI)
      addAuthorizationHeader(httpPut, userName, password)
      httpPut.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
      httpPut.setHeader("charset", "utf-8")
      val createMappingRequest = createMappingJsonAsStr(dataSourceNameToLowercase, fields, shards, replicas)
      println(createMappingRequest) //TODO: remove
      httpPut.setEntity(new StringEntity(createMappingRequest, ContentType.APPLICATION_JSON))

      // execute
      val createResponse = client.execute(httpPut)
      val createResponseAsString = getHttpResponseAsString(createResponse, httpPut)
      try {
        val createSuccessOpt = createResponseAsString.map(str => {
          implicit val formats = DefaultFormats
          (parse(str) \ "acknowledged").extract[Boolean]
        })
        createSuccessOpt match {
          case Some(success) =>
            if (!success) {
              println(s"Failed to create the mapping for DataSource $dataSourceName! Response: ${createResponseAsString.orNull}")
              throw new Exception(s"Failed to create the mapping for DataSource $dataSourceName! Response: ${createResponseAsString.orNull}")
            }
          case _ =>
            println(s"Failed to create the mapping for DataSource $dataSourceName! Response: ${createResponseAsString.orNull}")
            throw new Exception(s"Failed to create the mapping for DataSource $dataSourceName! Response: ${createResponseAsString.orNull}")
        }
      } catch {
        case parseError: Throwable =>
          println(s"Failed to create the mapping for DataSource $dataSourceName!")
          parseError.printStackTrace()
          throw new Exception(s"Failed to create the mapping for DataSource $dataSourceName!", parseError)
      }
      true
    } finally {
      client.close()
    }

  }

  /**
    * Helper method used to parse the HTTP Response into a String
    *
    * @param response the Htto Response Object
    * @param request the Http Request (either GET or POST)
    * @return a String
    */
  private def getHttpResponseAsString(response:CloseableHttpResponse, request:HttpUriRequest):Option[String] = {
    try {
      val entity = response.getEntity
      val responseString = {
        if (entity != null) {
          EntityUtils.toString(entity, "UTF-8")
        } else {
          ""
        }
      }
      val statusLine = response.getStatusLine
      if (statusLine.getStatusCode != HttpStatus.SC_OK) {
        println(s"The HTTP Request failed with code ${statusLine.getStatusCode} for URL: ${request.getURI}")
        return None
      }
      Option(responseString)
    } finally {
      response.close()
    }
  }

  /**
    * Get the Mapping JSON as a Str
    *
    * @param datSourceName the name of the datasource
    * @param fields the es fields to create
    * @return a json str
    */
  private def createMappingJsonAsStr(datSourceName:String, fields:Array[EsField], shards:Int=3, replicas:Int=1):String = {
    s"""
       |{
       |  "settings": {
       |     "number_of_shards" : $shards,
       |     "auto_expand_replicas" : 0,
       |     "number_of_replicas" : $replicas
       |  },
       |	"mappings": {
       |		"$datSourceName": {
       |			"properties": {
       |       ${createFieldMappingJsonAsStr(fields)}
       |			}
       |		}
       |	}
       |}
      """.stripMargin
  }

  private def createFieldMappingJsonAsStr(fields:Array[EsField]):String = {
    fields.foldRight("")( (field, str) => {
      val fieldJson = field.fieldType match {
        case EsFieldType.Unknown => ""
        case EsFieldType.GeoPoint =>
          s"""
            "${field.name}": {
              "type": "geo_point"
            }
          """.stripMargin
        case EsFieldType.GeoShape =>
          s"""
            "${field.name}": {
              "type": "geo_shape",
              "tree": "quadtree"
            }
          """.stripMargin
        case EsFieldType.Keyword =>
          s"""
            "${field.name}": {
              "type": "keyword"
            }
          """.stripMargin
        case _ =>
          s"""
            "${field.name}": {
              "type": "${field.fieldType.name}"
            }
          """.stripMargin
      }
      if (str != "")
        str + "," + fieldJson
      else
        str + fieldJson
    })
  }

  private def addAuthorizationHeader(httpRequest:HttpRequestBase, userName:String, password:String):Unit = {
    // credentials
    val authString = userName + ":" + password
    val credentialString: String = Base64.encodeBase64String(authString.getBytes)
    httpRequest.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentialString)
  }
}

/**
  * EsFieldType
  */
sealed trait EsFieldType {
  def name: String
  override def toString = name
}

object EsFieldType {
  case object Keyword extends EsFieldType {
    val name: String = "keyword"
  }
  case object Date extends EsFieldType {
    val name: String = "date"
  }
  case object Double extends EsFieldType {
    val name: String = "double"
  }
  case object Float extends EsFieldType {
    val name: String = "float"
  }
  case object GeoPoint extends EsFieldType {
    val name: String = "geo_point"
  }
  case object GeoShape extends EsFieldType {
    val name: String = "geo_shape"
  }
  case object Integer extends EsFieldType {
    val name: String = "integer"
  }
  case object Long extends EsFieldType {
    val name: String = "long"
  }
  case object Short extends EsFieldType {
    val name: String = "short"
  }
  case object Boolean extends EsFieldType {
    val name: String = "boolean"
  }
  case object Binary extends EsFieldType {
    val name: String = "binary"
  }
  case object Unknown extends EsFieldType {
    val name: String = "unknown"
  }

  def fromString(str:String):EsFieldType = {
    str.toLowerCase match {
      case Keyword.name => Keyword
      case Date.name => Date
      case Double.name => Double
      case Float.name => Float
      case GeoPoint.name => GeoPoint
      case GeoShape.name => GeoShape
      case Integer.name => Integer
      case Long.name => Long
      case Short.name => Short
      case Boolean.name => Boolean
      case Binary.name => Binary
      case _ => Unknown
    }
  }
}

/**
  * EsField
  *
  * @param name name of the field
  * @param fieldType the field type
  */
case class EsField(name:String, fieldType:EsFieldType)
