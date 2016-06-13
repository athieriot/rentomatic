package controllers

import java.time.LocalDate
import java.time.temporal.ChronoUnit._

import base.ApiMockApplication
import models.Movie
import org.specs2.matcher.JsonMatchers
import org.specs2.mock.Mockito
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}
import services.MovieCatalogue

import scala.concurrent.Future._

class MovieApiTest extends PlaySpecification with JsonMatchers with Mockito with ApiMockApplication {

  sequential

  private val matrix: Movie = Movie(1, "Matrix 11", Some(LocalDate.now().minus(6, DAYS)))
  private val spidy: Movie = Movie(2, "Spider Man", Some(LocalDate.now().minus(2, YEARS)))

  "The search API" should {

    "propose to search for movies to rent" in new WithApplication(injectable) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      mockMovieCatalogue.search("matrix") returns successful(List(matrix))

      val Some(result) = route(app, FakeRequest(GET, "/api/movie/search?query=matrix"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("title" -> "Matrix 11")
      contentAsString(result) must */("release_date" -> LocalDate.now().minus(6, DAYS).toString)
    }

    "not be able to search an empty query" in new WithApplication(injectable) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      val Some(result) = route(app, FakeRequest(GET, "/api/movie/search"))

      status(result) must equalTo(BAD_REQUEST)
      there was no(mockMovieCatalogue).search(anyString)
    }

    "handle empty results" in new WithApplication(injectable) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      mockMovieCatalogue.search("blabla") returns successful(List())

      val Some(result) = route(app, FakeRequest(GET, "/api/movie/search?query=blabla"))

      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must contain("No movies found for your query")
    }
  }

  "The popular movies API" should {

    "be able to show most popular movies" in new WithApplication(injectable) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      mockMovieCatalogue.popular() returns successful(List(matrix, spidy))

      val Some(result) = route(app, FakeRequest(GET, "/api/movie/popular"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("title" -> "Matrix 11")
      contentAsString(result) must */("title" -> "Spider Man")
    }

    "handle empty results" in new WithApplication(injectable) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      mockMovieCatalogue.popular() returns successful(List())

      val Some(result) = route(app, FakeRequest(GET, "/api/movie/popular"))

      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must contain("No movies found for your query")
    }
  }
}
