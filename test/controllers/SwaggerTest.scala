package controllers

import org.specs2.matcher.JsonMatchers
import play.api.test._

class SwaggerTest extends PlaySpecification with JsonMatchers {

  "The Swagger API" should {
    "produce a Swagger doc file" in new WithApplication {
      val Some(result) = route(app, FakeRequest(GET, "/docs/swagger.json"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("description" -> "All the rentals")
      contentAsString(result) must /("paths") /("/api/search") */("Search for movies")
    }
  }
}
