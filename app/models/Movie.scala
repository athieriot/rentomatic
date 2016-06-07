package models

import java.time.LocalDate
import java.time.LocalDate.now

import play.api.libs.json.Json

object ReleaseType extends Enumeration {
  type ReleaseType = Value
  val NEW_RELEASE, REGULAR_RELEASE, OLD_RELEASE = Value
}

case class Movie(title: String, releaseDate: LocalDate) {
  import ReleaseType._

  private val NEW_RELEASE_MONTHS = 6
  private val OLD_RELEASE_YEARS = 15

  val movieType = releaseDate match {
    case recent if releaseDate.isAfter(now().minusMonths(NEW_RELEASE_MONTHS)) => NEW_RELEASE
    case old if releaseDate.isBefore(now().minusYears(OLD_RELEASE_YEARS))     => OLD_RELEASE
    case regular                                                              => REGULAR_RELEASE
  }
}

object Movie {

  implicit val movieFormat = Json.format[Movie]
}