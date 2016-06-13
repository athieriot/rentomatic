package models

import java.time.LocalDate
import java.time.LocalDate.now

import play.api.libs.json._

object ReleaseType extends Enumeration {
  type ReleaseType = Value
  val NEW_RELEASE, REGULAR_RELEASE, OLD_RELEASE, NOT_RELEASED = Value

  implicit val releaseTypeFormat = new Format[ReleaseType] {
    def reads(json: JsValue) = JsSuccess(ReleaseType.withName(json.as[String]))
    def writes(releaseType: ReleaseType) = JsString(releaseType.toString)
  }

  /**
    * Core rule for Bonus points calculation
    *
    * @param releaseType a release Type Enumeration value
    * @return the number of Bonus points earned
    */
  def bonus(releaseType: ReleaseType): Int = { releaseType match {
    case NOT_RELEASED => 0
    case NEW_RELEASE  => 2
    case _            => 1
  }}
}

case class Movie(id: Long,
                 title: String,
                 release_date: Option[LocalDate],
                 adult: Boolean = false,
                 poster_path: Option[String] = None) {
  import ReleaseType._

  private val NEW_RELEASE_MONTHS = 6
  private val OLD_RELEASE_YEARS = 15

  /**
    * Core rule to identify which type of release a movie is.
    * This will greatly change the pricing applied.
    *
    * For now, the periods are hard coded but making them configurable would be nicer to handle inflation.
    */
  val releaseType = release_date match {
    case Some(recent) if recent.isAfter(now().minusMonths(NEW_RELEASE_MONTHS)) => NEW_RELEASE
    case Some(old) if old.isBefore(now().minusYears(OLD_RELEASE_YEARS))        => OLD_RELEASE
    case Some(regular)                                                         => REGULAR_RELEASE
    case None                                                                  => NOT_RELEASED
  }
}

object Movie {

  implicit val movieFormat = Json.format[Movie]
}