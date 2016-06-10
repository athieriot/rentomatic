package models

import java.time.LocalDate
import java.time.temporal.ChronoUnit._

import org.specs2.mutable.Specification

class RentalTest extends Specification {

  "The Rental Engine" should {

    "be able to compute new movie pricing" in {
      val matrix = Movie(1, "Matrix 11", Some(LocalDate.now().minus(6, DAYS)))

      Rental(matrix, 5).price must beEqualTo(200.0)
    }

    "be able to compute regular movies pricing" in {
      val spidy = Movie(1, "Spider Man", Some(LocalDate.now().minus(2, YEARS)))

      Rental(spidy, 5).price must beEqualTo(90.0)
      Rental(spidy, 1).price must beEqualTo(30.0)
    }

    "be able to compute old movies pricing" in {
      val outOfAfrica = Movie(1, "Out of Africa", Some(LocalDate.now().minus(20, YEARS)))

      Rental(outOfAfrica, 7).price must beEqualTo(90.0)
      Rental(outOfAfrica, 3).price must beEqualTo(30.0)
    }

    "not charge for a 0 day rental" in {
      val outOfAfrica = Movie(1, "Out of Africa", Some(LocalDate.parse("1986-03-26")))

      Rental(outOfAfrica, 0).price must beEqualTo(0.0)
    }

    "not charge for unreleased movie" in {
      val xmen = Movie(1, "X-Men 32", None)

      Rental(xmen, 4).price must beEqualTo(0.0)
    }
  }
}
