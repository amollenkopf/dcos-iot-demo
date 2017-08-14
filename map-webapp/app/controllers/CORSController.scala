package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc._

@Singleton
class CORSController @Inject()() extends Controller {
  def headers = List(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS, DELETE, PUT, HEAD",
    "Access-Control-Max-Age" -> "3600",
    "Access-Control-Allow-Headers" -> "Origin, Content-Type, content-type, Accept, Authorization, x-requested-with",
    "Access-Control-Allow-Credentials" -> "true"
  )

  def rootOptions = options("/")

  def options(url: String) = Action { _ =>
    NoContent.withHeaders(headers: _*)
  }
}
