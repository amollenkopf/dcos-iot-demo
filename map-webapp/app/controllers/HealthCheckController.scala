package controllers

import javax.inject.{Inject, Singleton}

import play.api.http.ContentTypes
import play.api.mvc.{Action, Controller}

@Singleton
class HealthCheckController @Inject() extends Controller {

  def healthcheck() = Action { httpRequest =>
    Ok(s"Healthy").withHeaders(ACCESS_CONTROL_ALLOW_ORIGIN -> "*").as(ContentTypes.TEXT)
  }
}
