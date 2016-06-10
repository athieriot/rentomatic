package models

import java.time.LocalDate
import java.time.temporal.ChronoUnit._

import models.ReleaseType._
import play.api.test.PlaySpecification


class MovieTest extends PlaySpecification {

  "A Movie" should {

    "not be release if there is no release date" in {
      Movie(1, "X-Men 26", None).releaseType must beEqualTo(NOT_RELEASED)
    }

    "be a new release if younger than 6 months" in {
      Movie(1, "Matrix 11", Some(LocalDate.now().minus(6, DAYS))).releaseType must beEqualTo(NEW_RELEASE)
    }

    "be an old release if older than 15 years" in {
      Movie(1, "Out of Africa", Some(LocalDate.now().minus(20, YEARS))).releaseType must beEqualTo(OLD_RELEASE)
    }

    "be a regular release between 15 years and 6 months" in {
      Movie(1, "Spider Man", Some(LocalDate.now().minus(2, YEARS))).releaseType must beEqualTo(REGULAR_RELEASE)
    }

    "reward with bonus points based on Release Types" in {

      List(REGULAR_RELEASE, NOT_RELEASED, NEW_RELEASE).map(bonus).sum should beEqualTo(3)
      List(NEW_RELEASE, NOT_RELEASED, NEW_RELEASE).map(bonus).sum should beEqualTo(4)
      List(REGULAR_RELEASE, NOT_RELEASED).map(bonus).sum should beEqualTo(1)
    }
  }
}
