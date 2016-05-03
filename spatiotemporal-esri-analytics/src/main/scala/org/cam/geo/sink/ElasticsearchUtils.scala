package org.cam.geo.sink

import java.net.URL
import java.util.UUID

import com.google.common.net.HttpHeaders
import org.apache.http.HttpStatus
import org.apache.http.client.methods.{HttpGet, HttpPost, _}
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
  def doesDataSourceExists(dataSourceName:String, esHostName:String, esPort:Int = 9200):Boolean = {
    val client = HttpClients.createDefault()
    try {
      val dataSourceNameToLowercase = dataSourceName.toLowerCase()
      val existsURLStr = s"http://$esHostName:$esPort/$dataSourceNameToLowercase"
      val existsURL:URL = new URL(existsURLStr)
      val httpGet:HttpGet = new HttpGet(existsURL.toURI)

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
    * @param geometryType the geometry type, could be "esriGeometryPoint", "esriGeometryPolygon" or "esriGeometryPolyline"
    */
  def createDataSource(dataSourceName:String, fields:Array[EsField], esHostName:String, esPort:Int = 9200, geometryType:String = "esriGeometryPoint"):Boolean = {
    val client = HttpClients.createDefault()
    try {
      val dataSourceNameToLowercase = dataSourceName.toLowerCase()
      // step 1 - create the mapping with metadata
      val createMappingURLStr = s"http://$esHostName:$esPort/$dataSourceNameToLowercase"
      val createMappingURL:URL = new URL(createMappingURLStr)
      val httpPut:HttpPut = new HttpPut(createMappingURL.toURI)
      httpPut.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
      httpPut.setHeader("charset", "utf-8")
      val createMappingRequest = createMappingJsonAsStr(dataSourceNameToLowercase, fields)
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
              println(s"Failed to create the mapping for DataSource $dataSourceName!")
              return false
            }
          case _ =>
            println(s"Failed to create the mapping for DataSource $dataSourceName!")
            return false
        }
      } catch {
        case parseError: Throwable =>
          println(s"Failed to create the mapping for DataSource $dataSourceName!")
          parseError.printStackTrace()
          return false
      }

      // step 2 - create the metadata documents
      val geometryField = fields.find( field => field.fieldType == EsFieldType.GeoPoint || field.fieldType == EsFieldType.GeoShape )
      val geometryFieldName = geometryField match {
        case Some(field) if field.fieldType == EsFieldType.GeoPoint || field.fieldType == EsFieldType.GeoShape=>
          field.name
        case _ =>
          "Geometry"
      }
      val createOIDDoc =
        s"""{ "index" : { "_index" : "$dataSourceNameToLowercase", "_type" : "---metadata---", "_id" : "object_id_generator" } }
            {}
         """.stripMargin
      val createMetadataDoc =
        s"""{ "index" : { "_index" : "$dataSourceNameToLowercase", "_type" : "---metadata---", "_id" : "data_source_metadata" } }
            {"aliasName":"$dataSourceNameToLowercase","indexNamePrefix":"${UUID.randomUUID().toString}","indexTypeName":"$dataSourceNameToLowercase","oidFieldName":"objectid","globalIdFieldName":"globalid","trackIdFieldName":null,"startTimeFieldName":null,"endTimeFieldName":null,"geometryFieldName":"$geometryFieldName","geometryType":"$geometryType","timeInterval":10,"timeIntervalUnits":"esriTimeUnitsSeconds","hasLiveData":true,"objectIdStrategy":"ObjectId64Bit","rollingIndexStrategy":"Hourly","oidSeed":0,"oidBlockSize":1,"esriGeoHashes":"[{\\"field_name\\":\\"triangle_h_102100\\",\\"type\\":\\"triangle\\",\\"wkid\\":102100,\\"precision\\":20,\\"orientation\\":\\"horizontal\\"},{\\"field_name\\":\\"triangle_v_102100\\",\\"type\\":\\"triangle\\",\\"wkid\\":102100,\\"precision\\":20,\\"orientation\\":\\"vertical\\"},{\\"field_name\\":\\"square_102100\\",\\"type\\":\\"square\\",\\"wkid\\":102100,\\"precision\\":20}]"}
        """.stripMargin
      val bulkRequest = createOIDDoc + "\n" +createMetadataDoc
      val bulkURLStr = s"http://$esHostName:$esPort/$dataSourceNameToLowercase/_bulk"
      val bulkURL:URL = new URL(bulkURLStr)
      val httpPost:HttpPost = new HttpPost(bulkURL.toURI)
      httpPost.setEntity(new StringEntity(bulkRequest, ContentType.DEFAULT_TEXT))

      // execute
      val bulkResponse = client.execute(httpPost)
      val bulkResponseAsString = getHttpResponseAsString(bulkResponse, httpPut)
      try {
        val createSuccessOpt = bulkResponseAsString.map(str => {
          implicit val formats = DefaultFormats
          (parse(str) \ "errors").extract[Boolean]
        })
        createSuccessOpt match {
          case Some(errors) =>
            if (errors) {
              println(s"Failed to create the metadata and oid documents for DataSource $dataSourceName! Errors: $bulkResponseAsString")
              return false
            }
          case _ =>
            println(s"Failed to create the mapping for DataSource $dataSourceName! Errors: $bulkResponseAsString")
            return false
        }
      } catch {
        case parseError: Throwable =>
          println(s"Failed to create the mapping for DataSource $dataSourceName!")
          parseError.printStackTrace()
          return false
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
    // make sure we close the response
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
  private def createMappingJsonAsStr(datSourceName:String, fields:Array[EsField]):String = {
    s"""
       |{
       |	"mappings": {
       |		"$datSourceName": {
       |			"_timestamp": {
       |				"enabled": true
       |			},
       |			"properties": {
       |       ${createFieldMappingJsonAsStr(fields)}
       |			}
       |		},
       |		"---metadata---": {
       |			"_timestamp": {
       |				"enabled": true
       |			},
       |			"properties": {
       |				"aliasName": {
       |					"type": "string"
       |				},
       |				"endTimeFieldName": {
       |					"type": "string"
       |				},
       |				"esriGeoHashes": {
       |					"type": "string"
       |				},
       |				"geometryFieldName": {
       |					"type": "string"
       |				},
       |				"geometryType": {
       |					"type": "string"
       |				},
       |				"globalIdFieldName": {
       |					"type": "string"
       |				},
       |				"hasLiveData": {
       |					"type": "string"
       |				},
       |				"indexNamePrefix": {
       |					"type": "string"
       |				},
       |				"indexTypeName": {
       |					"type": "string"
       |				},
       |				"objectIdStrategy": {
       |					"type": "string"
       |				},
       |				"oidBlockSize": {
       |					"type": "string"
       |				},
       |				"oidFieldName": {
       |					"type": "string"
       |				},
       |				"oidGuid": {
       |					"type": "string"
       |				},
       |				"oidSeed": {
       |					"type": "string"
       |				},
       |				"rollingIndexStrategy": {
       |					"type": "string"
       |				},
       |				"startTimeFieldName": {
       |					"type": "string"
       |				},
       |				"timeInterval": {
       |					"type": "string"
       |				},
       |				"timeIntervalUnits": {
       |					"type": "string"
       |				},
       |				"trackIdFieldName": {
       |					"type": "string"
       |				}
       |			}
       |		}
       |	}
       |}
      """.stripMargin
  }

  private def createFieldMappingJsonAsStr(fields:Array[EsField]):String = {
    val idFields =
      """
        |"globalid": {
        |  "type": "string"
        |},
        |"objectid": {
        |  "type": "long"
        |}
      """.stripMargin
    fields.foldRight(idFields)( (field, str) => {
      val fieldJson = field.fieldType match {
        case EsFieldType.Unknown => ""
        case EsFieldType.GeoPoint =>
          s"""
            "${field.name}": {
              "type": "geo_point",
              "lat_lon": "true",
              "geohash": "true",
              "geohash_prefix": "true"
            },
            "---geo_hash---": {
              "type": "esri_geo_hash",
              "hashes": [{
                "orientation": "horizontal",
                "precision": 20,
                "wkid": 102100,
                "type": "triangle",
                "field_name": "triangle_h_102100"
              },
              {
                "orientation": "vertical",
                "precision": 20,
                "wkid": 102100,
                "type": "triangle",
                "field_name": "triangle_v_102100"
              },
              {
                "precision": 20,
                "wkid": 102100,
                "type": "square",
                "field_name": "square_102100"
              }]
            }
          """.stripMargin
        case EsFieldType.GeoShape =>
          s"""
            "${field.name}": {
              "type": "geo_shape",
              "tree": "quadtree"
            }
          """.stripMargin
        case EsFieldType.String =>
          s"""
            "${field.name}": {
              "type": "string",
              "index": "not_analyzed"
            }
          """.stripMargin
        case _ =>
          s"""
            "${field.name}": {
              "type": "${field.fieldType.name}"
            }
          """.stripMargin
      }
      str + "," + fieldJson
    })
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
  case object String extends EsFieldType {
    val name: String = "string"
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
      case String.name => String
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
