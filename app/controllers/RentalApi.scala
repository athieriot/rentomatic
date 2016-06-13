package controllers

import javax.inject.Inject

import models.Rental
import play.api.libs.json.Json._
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{Action, BodyParsers, Controller}
import repositories.InvoiceRepository
import services.MovieCatalogue

import scala.concurrent.Future._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Once a customer found a movie, he will be able to request for a preview of the pricing
  * (Given a movie id and a number of days he would like to)
  *
  * Then a proper rental will result in an invoice which going to be persisted in order to follow returns later on.
  *
  * @param movieCatalogue movie service provider
  * @param invoiceRepository invoice repository
  * @param context implicit asynchronous context
  */
class RentalApi @Inject()(movieCatalogue: MovieCatalogue,
                          invoiceRepository: InvoiceRepository)(implicit val context: ExecutionContext) extends Controller {

  def pricing(id: Long, days: Int) = Action.async {
    invoicing(RentalRequest(id, days)).map { rental =>

      Ok(Json.obj("rental" -> toJson(rental), "price" -> s"${rental.price} SEK"))
    } recover {
      case e: NoSuchElementException => NotFound(toJson(e.getLocalizedMessage))
    }
  }

  case class RentalRequest(id: Long, days: Int)

  implicit val rentalFormat = Json.format[RentalRequest]

  def invoice = Action.async(BodyParsers.parse.json) { request =>
    val result = request.body.validate[List[RentalRequest]]
    result.fold(
      errors => successful(BadRequest(JsError.toJson(errors))),
      rentalRequests => {

        invoicing(rentalRequests)
          .flatMap(invoiceRepository.save)
          .map(pairs => {
            val (rentals, invoices) = pairs.unzip

            Created(Json.obj(
              "rentals" -> toJson(rentals),
              "invoices" -> toJson(invoices),
              "total" -> s"${invoices.map(_.paid).sum} SEK"
            ))
          })
          .recover {
            case e: NoSuchElementException => NotFound(toJson(e.getLocalizedMessage))
          }
      }
    )
  }

  def invoices = Action.async {
    invoiceRepository.invoices().map { invoices => Ok(toJson(invoices)) }
  }

  private def invoicing(rentalRequests: List[RentalRequest]): Future[List[Rental]] = {
    sequence(rentalRequests.map(invoicing))
  }

  // findById is called multiple times because there is not (as I know of) any API to requests more than one movie at a time
  private def invoicing(rentalRequest: RentalRequest): Future[Rental] = {
    movieCatalogue.findById(rentalRequest.id).flatMap {
      case None =>        failed(new NoSuchElementException(s"Movie ${rentalRequest.id} not found"))
      case Some(movie) => successful(Rental(movie, rentalRequest.days))
    }
  }
}