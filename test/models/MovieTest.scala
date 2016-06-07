package models

import java.time.LocalDate
import java.time.temporal.ChronoUnit._

import models.ReleaseType._
import play.api.test.PlaySpecification


class MovieTest extends PlaySpecification {

  "A Movie" should {

    "be a new release if younger than 6 months" in {
      Movie("Matrix 11", LocalDate.now().minus(6, DAYS)).movieType should beEqualTo(NEW_RELEASE)
    }

    "be an old release if older than 15 years" in {
      Movie("Out of Africa", LocalDate.now().minus(20, YEARS)).movieType should beEqualTo(OLD_RELEASE)
    }

    "be a regular release between 15 years and 6 months" in {
      Movie("Spider Man", LocalDate.now().minus(2, YEARS)).movieType should beEqualTo(REGULAR_RELEASE)
    }
  }
}
