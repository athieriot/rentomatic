package controllers

import javax.inject.Inject

import models.Movie
import models.ReleaseType._
import play.api.libs.json.Json._
import play.api.mvc.{Action, Controller}
import repositories.InvoiceRepository
import services.MovieCatalogue

import scala.concurrent.ExecutionContext

/** ************
  * Start here !
  * ************
  *
  * Searching for a movie a customer would like to rent.
  * A movie can be searched by title or by the most popular.
  *
  * On top of the title or the release date (And a post URL !),
  * a movie entity includes a unique ID that will be used across the rest of the API.
  *
  * @param movieCatalogue movie service provider
  * @param invoiceRepository invoice repository
  * @param context implicit asynchronous context
  */
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