package controllers

import javax.inject.Inject

import com.iheart.playSwagger.SwaggerSpecGenerator
import play.api.cache.Cached
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

class Swagger @Inject()(cached: Cached) extends Controller {
  implicit val cl = getClass.getClassLoader

  private lazy val generator = SwaggerSpecGenerator()

  def specs = cached("swaggerDef") {
    Action.async { _ =>
      Future.fromTry(generator.generate()).map(Ok(_))
    }
  }
}