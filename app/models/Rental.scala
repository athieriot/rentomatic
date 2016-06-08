package models

import play.api.libs.json.Json

case class Rental(movie: Movie, days: Int) {
  import ReleaseType._

  private val PREMIUM_PRICE: Int = 40
  private val BASIC_PRICE: Int = 30

  def price: Double = movie.movieType match {
    case _ if days == 0   => 0
    case NOT_RELEASED     => 0
    case NEW_RELEASE      => PREMIUM_PRICE * days
    case REGULAR_RELEASE  => BASIC_PRICE + BASIC_PRICE * (if (days - 3 < 0) 0 else days - 3)
    case OLD_RELEASE      => BASIC_PRICE + BASIC_PRICE * (if (days - 5 < 0) 0 else days - 5)
  }
}

object Rental {

  implicit val rentalFormat = Json.format[Rental]
}