package controllers

import javax.inject.Inject

import models.Movie
import models.ReleaseType._
import play.api.libs.json.Json._
import play.api.mvc.{Action, Controller}
import repositories.InvoiceRepository
import services.MovieCatalogue

import scala.concurrent.ExecutionContext

class MovieApi @Inject() (movieCatalogue: MovieCatalogue,
                          invoiceRepository: InvoiceRepository)(implicit val context: ExecutionContext) extends Controller {

  val onlyRentable: List[Movie] => List[Movie] = _.filterNot(_.adult).filter(_.releaseType != NOT_RELEASED)

  def popular = Action.async {
    movieCatalogue.popular().map(onlyRentable).map {
      case Nil => NotFound("No movies found for your query")
      case results => Ok(toJson(results))
    }
  }

  def search(query: String) = Action.async {
    movieCatalogue.search(query).map(onlyRentable).map {
      case Nil => NotFound("No movies found for your query")
      case results => Ok(toJson(results))
    }
  }
}