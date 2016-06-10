package controllers

import javax.inject.Inject

import com.iheart.playSwagger.SwaggerSpecGenerator
import play.api.Configuration
import play.api.cache.Cached
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsString
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

class Swagger @Inject()(cached: Cached,
                        config: Configuration) extends Controller {
  implicit val cl = getClass.getClassLoader

  private lazy val generator = SwaggerSpecGenerator()

  def specs = cached("swaggerDef") {
    Action.async { _ =>
      val hostname = config.getString("play.swagger.host").getOrElse("localhost:9000")
      val scheme = config.getString("play.swagger.scheme").getOrElse("http")

      Future.fromTry(generator.generate().map(_ + ("host" -> JsString(hostname)) + ("scheme" -> JsString(scheme)))).map(Ok(_))
    }
  }
}