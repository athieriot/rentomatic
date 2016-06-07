package models

import java.time.LocalDate
import java.time.temporal.ChronoUnit._

import org.specs2.mutable.Specification

class RentalTest extends Specification {

  "The Rental Engine" should {

    "be able to compute new movie pricing" in {
      val matrix = Movie("Matrix 11", LocalDate.now().minus(6, DAYS))

      Rental(matrix, 5).price should beEqualTo(200.0)
    }

    "be able to compute regular movies pricing" in {
      val spidy = Movie("Spider Man", LocalDate.now().minus(2, YEARS))

      Rental(spidy, 5).price should beEqualTo(90.0)
      Rental(spidy, 1).price should beEqualTo(30.0)
    }

    "be able to compute old movies pricing" in {
      val outOfAfrica = Movie("Out of Africa", LocalDate.now().minus(20, YEARS))

      Rental(outOfAfrica, 7).price should beEqualTo(90.0)
      Rental(outOfAfrica, 3).price should beEqualTo(30.0)
    }

    "not charge for a 0 day rental" in {
      val outOfAfrica = Movie("Out of Africa", LocalDate.parse("1986-03-26"))

      Rental(outOfAfrica, 0).price should beEqualTo(0.0)
    }
  }
}
