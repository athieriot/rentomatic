package controllers

import org.specs2.matcher.JsonMatchers
import play.api.test._

class ApiTest extends PlaySpecification with JsonMatchers {

  "The API" should {
    "propose a list of movies to rent" in new WithApplication {
      val Some(result) = route(app, FakeRequest(GET, "/api/movies"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("Matrix")
    }

    "be able to compute invoice pricing" in new WithApplication {
      val Some(result) = route(app, FakeRequest(GET, "/api/invoice?movies=Matrix&days=2"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must /("80.0 SEK")
    }
  }
}
