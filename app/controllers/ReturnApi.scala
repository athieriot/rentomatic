package controllers

import java.time.Instant
import javax.inject.Inject

import models.{Invoice, ReleaseType}
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import repositories.InvoiceRepository
import services.MovieCatalogue

import scala.concurrent.Future.{sequence, successful}
import scala.concurrent.{ExecutionContext, Future}

//TODO: Add basic comments
class ReturnApi @Inject()(movieCatalogue: MovieCatalogue,
                          invoiceRepository: InvoiceRepository)(implicit val context: ExecutionContext) extends Controller {

  def returns(returnDate: Option[String]) = Action.async(BodyParsers.parse.json) { request =>
    val result = request.body.validate[List[Long]]
    result.fold(
      errors => successful(BadRequest(JsError.toJson(errors))),
      movieIds => {

        invoiceRepository
          .findByMovieIds(movieIds)
          .flatMap {
            case x if x.size < movieIds.size => successful(NotFound(s"Unable to find the following movies: ${movieIds.diff(x.map(_.movieId)).mkString(", ")}"))
            case x if x.exists(_.isComplete) => successful(Conflict(s"The following movies were already returned: ${x.filter(_.isComplete).map(_.movieId).mkString(", ")}"))
            case x =>
              val completed: Seq[Future[Invoice]] = x.map(_.complete(returnDate.map(Instant.parse(_)))).map(invoiceRepository.update)
              sequence(completed).map { invoices =>

                Ok(Json.obj(
                  "invoices" -> toJson(invoices),
                  "extraCharge" -> s"${invoices.map(_.extraCharge.getOrElse(0.0)).sum} SEK"
                ))
              }
          }
      }
    )
  }

  def bonus = Action.async { request =>
    invoiceRepository.invoices().map { invoices =>

      Ok(Json.obj(
        "points" -> invoices.map(_.releaseType).map(ReleaseType.bonus).sum
      ))
    }
  }
}