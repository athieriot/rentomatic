import javax.inject.Singleton

import play.api.http.HttpErrorHandler
import play.api.libs.json.Json.toJson
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent._

@Singleton
class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)(toJson("A client error occurred: " + message))
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    Future.successful(
      InternalServerError(toJson("A server error occurred: " + exception.getMessage))
    )
  }
}