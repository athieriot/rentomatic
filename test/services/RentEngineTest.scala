package services

import org.specs2.mutable.Specification

class RentEngineTest extends Specification {

  "The Rental Engine" should {

    "be able to compute new movie pricing" in {
      new RentEngine().rentalPrice("Matrix", 5) should beEqualTo(200.0)
    }

    "be able to compute regular movies pricing" in {
      new RentEngine().rentalPrice("Spider Man", 5) should beEqualTo(90.0)
      new RentEngine().rentalPrice("Spider Man", 1) should beEqualTo(30.0)
    }

    "be able to compute old movies pricing" in {
      new RentEngine().rentalPrice("Out of Africa", 7) should beEqualTo(90.0)
      new RentEngine().rentalPrice("Out of Africa", 3) should beEqualTo(30.0)
    }

    "not charge for a 0 day rental" in {
      new RentEngine().rentalPrice("Out of Africa", 0) should beEqualTo(0.0)
    }
  }
}
