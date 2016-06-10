package controllers

import java.time.Instant
import javax.inject.Inject

import models.ReleaseType.NOT_RELEASED
import models.{Invoice, Movie, Rental}
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import repositories.InvoiceRepository
import services.TMDBApi

import scala.concurrent.Future.{failed, sequence, successful}
import scala.concurrent.{ExecutionContext, Future}

//TODO: Add basic comments
//TODO: Split the class? As well as the test one
class Api @Inject() (tmdbApi: TMDBApi,
                     invoiceRepository: InvoiceRepository
                     )(implicit val context: ExecutionContext) extends Controller {

  val onlyRentable: List[Movie] => List[Movie] = _.filterNot(_.adult).filter(_.releaseType != NOT_RELEASED)

  def popular = Action.async {
    tmdbApi.popular().map(onlyRentable).map {
      case Nil => NotFound("No movies found for your query")
      case results => Ok(toJson(results))
    }
  }

  def search(query: String) = Action.async {
    tmdbApi.search(query).map(onlyRentable).map {
      case Nil => NotFound("No movies found for your query")
      case results => Ok(toJson(results))
    }
  }

  def pricing(id: Long, days: Int) = Action.async {
    invoicing(RentalRequest(id, days)).map { rental =>

      Ok(Json.obj("rental" -> toJson(rental), "price" -> s"${rental.price} SEK"))
    } recover {
      case e: NoSuchElementException => NotFound(e.getLocalizedMessage)
    }
  }

  case class RentalRequest(id: Long, days: Int)

  implicit val rentalFormat = Json.format[RentalRequest]

  //TODO: Better error management for other Exception (Such as SQL exceptions)
  def invoice = Action.async(BodyParsers.parse.json) { request =>
    val result = request.body.validate[List[RentalRequest]]
    result.fold(
      errors => successful(BadRequest(JsError.toJson(errors))),
      rentalRequests => {

        invoicing(rentalRequests)
          .flatMap(invoiceRepository.save)
          .map(pairs => {
            val (rentals, invoices) = pairs.unzip

            Ok(Json.obj(
              "rentals" -> toJson(rentals),
              "invoices" -> toJson(invoices),
              "total" -> s"${invoices.map(_.paid).sum} SEK"
            ))
          })
          .recover {
            case e: NoSuchElementException => NotFound(e.getLocalizedMessage)
          }
      }
    )
  }

  def invoices = Action.async {
    invoiceRepository.invoices().map { invoices => Ok(toJson(invoices)) }
  }

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

  private def invoicing(rentalRequests: List[RentalRequest]): Future[List[Rental]] = {
    sequence(rentalRequests.map(invoicing))
  }

  // findById is called multiple times because there is not (as I know of) any API to requests more than one movie at a time
  private def invoicing(rentalRequest: RentalRequest): Future[Rental] = {
    tmdbApi.findById(rentalRequest.id).flatMap {
      case None =>        failed(new NoSuchElementException(s"Movie ${rentalRequest.id} not found"))
      case Some(movie) => successful(Rental(movie, rentalRequest.days))
    }
  }
}