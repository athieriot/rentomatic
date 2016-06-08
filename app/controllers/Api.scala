package controllers

import javax.inject.Inject

import models.Rental
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import services.TMDBApi

import scala.concurrent.Future.{failed, sequence, successful}
import scala.concurrent.{ExecutionContext, Future}

class Api @Inject() (tmdbApi: TMDBApi,
                     implicit val context: ExecutionContext) extends Controller {

  def popular = Action.async {
    tmdbApi.popular().map(_.filterNot(_.adult)).map {
      case Nil => NotFound("No movies found for your query")
      case results => Ok(toJson(results))
    }
  }

  def search(query: String) = Action.async {
    tmdbApi.search(query).map(_.filterNot(_.adult)).map {
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

  def invoice = Action.async(BodyParsers.parse.json) { request =>
    val result = request.body.validate[List[RentalRequest]]
    result.fold(
      errors => successful(BadRequest(JsError.toJson(errors))),
      rentalRequests => {

        invoicing(rentalRequests).map { rentals =>

          Ok(Json.obj("rentals" -> toJson(rentals), "total" -> s"${rentals.map(_.price).sum} SEK"))
        } recover {
          case e: NoSuchElementException => NotFound(e.getLocalizedMessage)
        }
      }
    )
  }

  private def invoicing(rentalRequests: List[RentalRequest]): Future[List[Rental]] = {
    sequence(rentalRequests.map(invoicing))
  }

  private def invoicing(rentalRequest: RentalRequest): Future[Rental] = {
    tmdbApi.findById(rentalRequest.id).flatMap {
      case None =>        failed(new NoSuchElementException(s"Movie ${rentalRequest.id} not found"))
      case Some(movie) => successful(Rental(movie, rentalRequest.days))
    }
  }
}