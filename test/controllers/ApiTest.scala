package controllers

import org.specs2.matcher.JsonMatchers
import play.api.libs.json.Json
import play.api.test._

class ApiTest extends PlaySpecification with JsonMatchers {

  "The movies API" should {
    "propose a list of movies to rent" in new WithApplication {
      val Some(result) = route(app, FakeRequest(GET, "/api/movies"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("title" -> "Matrix 11")
      contentAsString(result) must */("releaseDate" -> "2016-06-23")
    }
  }

  "The pricing API" should {
    "be able to compute invoice pricing" in new WithApplication {
      val Some(result) = route(app, FakeRequest(GET, "/api/pricing?title=Matrix 11&days=2"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("80.0 SEK")
    }

    "warn the user if movie is not available" in new WithApplication {
      val Some(result) = route(app, FakeRequest(GET, "/api/pricing?title=Snuff"))

      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must contain("Movie Snuff not found")
    }
  }

  "The invoice API" should {
    "invoice a rental of several movies" in new WithApplication {
      val Some(result) = route(app, FakeRequest(POST, "/api/invoice").withJsonBody(Json.parse(
        """
          |[{
          |   "title": "Matrix 11",
          |   "days": 1
          |}, {
          |   "title": "Spider Man",
          |   "days": 5
          |}, {
          |   "title": "Spider Man 2",
          |   "days": 2
          |}, {
          |   "title": "Out of Africa",
          |   "days": 7
          |}]
        """.stripMargin)))

      status(result) must equalTo(OK)
      contentAsString(result) must */("total", "250.0 SEK")
    }

    "ignore unknown movies from invoice" in new WithApplication {
      val Some(result) = route(app, FakeRequest(POST, "/api/invoice").withJsonBody(Json.parse(
        """
          |[{
          |   "title": "Matrix 11",
          |   "days": 1
          |}, {
          |   "title": "Snuff",
          |   "days": 7
          |}]
        """.stripMargin)))

      status(result) must equalTo(OK)
      contentAsString(result) must */("total", "40.0 SEK")
    }

    "show request errors" in new WithApplication {
      val Some(result) = route(app, FakeRequest(POST, "/api/invoice").withJsonBody(Json.parse(
        """
          |[{
          |   "movieTitle": "Matrix 11"
          |}]
        """.stripMargin)))

      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must */("error.path.missing")
    }
  }
}
