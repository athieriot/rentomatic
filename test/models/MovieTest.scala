package models

import java.time.LocalDate
import java.time.temporal.ChronoUnit._

import models.ReleaseType._
import play.api.test.PlaySpecification


class MovieTest extends PlaySpecification {

  "A Movie" should {

    "not be release if there is no release date" in {
      Movie(1, "X-Men 26", None).movieType must beEqualTo(NOT_RELEASED)
    }

    "be a new release if younger than 6 months" in {
      Movie(1, "Matrix 11", Some(LocalDate.now().minus(6, DAYS))).movieType must beEqualTo(NEW_RELEASE)
    }

    "be an old release if older than 15 years" in {
      Movie(1, "Out of Africa", Some(LocalDate.now().minus(20, YEARS))).movieType must beEqualTo(OLD_RELEASE)
    }

    "be a regular release between 15 years and 6 months" in {
      Movie(1, "Spider Man", Some(LocalDate.now().minus(2, YEARS))).movieType must beEqualTo(REGULAR_RELEASE)
    }
  }
}
