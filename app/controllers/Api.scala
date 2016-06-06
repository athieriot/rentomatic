package controllers

import javax.inject.Inject

import play.api.libs.json.Json.toJson
import play.api.mvc._
import services.RentEngine

class Api @Inject() (engine: RentEngine) extends Controller {

  def movies = Action {
    Ok(toJson(List("Matrix", "Spider Man", "Spider Man 2", "Out of Africa")))
  }

  def invoice(movies: List[String], days: Int) = Action {

    val price = movies
      .map(engine.rentalPrice(_, days))
      .map(p => s"$p SEK")

    Ok(toJson(price))
  }
}