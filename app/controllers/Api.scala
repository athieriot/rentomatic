package controllers

import java.time.LocalDate

import models.{Movie, Rental}
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsError, Json}
import play.api.mvc._

class Api extends Controller {

  private val availableMovies = List(
    Movie("Matrix 11", LocalDate.parse("2016-06-23")),
    Movie("Spider Man", LocalDate.parse("2002-06-12")),
    Movie("Spider Man 2", LocalDate.parse("2004-07-14")),
    Movie("Out of Africa", LocalDate.parse("1986-03-26"))
  )

  def movies = Action {
    Ok(toJson(availableMovies))
  }

  def pricing(title: String, days: Int) = Action {
    invoicing(RentalRequest(title, days)) match {
      case Left(message) => NotFound(message)
      case Right(price) =>

        Ok(Json.obj("price" -> s"$price SEK"))
    }
  }

  case class RentalRequest(title: String, days: Int)

  implicit val rentalFormat = Json.format[RentalRequest]

  def invoice = Action(BodyParsers.parse.json) { request =>
    val result = request.body.validate[List[RentalRequest]]
    result.fold(
      errors => BadRequest(JsError.toJson(errors)),
      rentals => {
        val total = rentals.map(invoicing).filter(_.isRight).map(_.right.get).sum

        Ok(Json.obj("total" -> s"$total SEK"))
      }
    )
  }

  private def invoicing(rentalRequest: RentalRequest): Either[String, Double] = {
    availableMovies.find(_.title == rentalRequest.title) match {
      case None => Left(s"Movie ${rentalRequest.title} not found")
      case Some(movie) => Right(Rental(movie, rentalRequest.days).price)
    }
  }
}