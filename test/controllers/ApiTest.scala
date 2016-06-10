package controllers

import java.sql.Timestamp
import java.time.Instant.now
import java.time.temporal.ChronoUnit._
import java.time.{Instant, LocalDate}
import java.util.UUID
import java.util.UUID.randomUUID

import models.ReleaseType.{NEW_RELEASE, REGULAR_RELEASE}
import models.{Invoice, Movie, Rental}
import org.specs2.matcher.JsonMatchers
import org.specs2.mock._
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.{WithApplication, _}
import repositories.InvoiceRepository
import services.TMDBApi

import scala.concurrent.Future
import scala.concurrent.Future.successful


class ApiTest extends PlaySpecification with JsonMatchers with Mockito {

  sequential

  private val injectable = { builder: GuiceApplicationBuilder =>
    builder.overrides(
      bind[TMDBApi].toInstance(mock[TMDBApi]),
      bind[InvoiceRepository].toInstance(mock[InvoiceRepository])
    )
  }

  private val matrix: Movie = Movie(1, "Matrix 11", Some(LocalDate.now().minus(6, DAYS)))
  private val spidy: Movie = Movie(2, "Spider Man", Some(LocalDate.now().minus(2, YEARS)))
  private val spidy2: Movie = Movie(3, "Spider Man 2", Some(LocalDate.now().minus(2, YEARS)))
  private val outOfAfrica: Movie = Movie(4, "Out of Africa", Some(LocalDate.now().minus(20, YEARS)))

  "The search API" should {

    "propose to search for movies to rent" in new WithApplication(injectable) {
      val mockTMDBApi: TMDBApi = app.injector.instanceOf[TMDBApi]
      mockTMDBApi.search("matrix") returns successful(List(matrix))

      val Some(result) = route(app, FakeRequest(GET, "/api/search?query=matrix"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("title" -> "Matrix 11")
      contentAsString(result) must */("release_date" -> LocalDate.now().minus(6, DAYS).toString)
    }

    "not be able to search an empty query" in new WithApplication(injectable) {
      val mockTMDBApi: TMDBApi = app.injector.instanceOf[TMDBApi]
      val Some(result) = route(app, FakeRequest(GET, "/api/search"))

      status(result) must equalTo(BAD_REQUEST)
      there was no(mockTMDBApi).search(anyString)
    }

    "handle empty results" in new WithApplication(injectable) {
      val mockTMDBApi: TMDBApi = app.injector.instanceOf[TMDBApi]
      mockTMDBApi.search("blabla") returns successful(List())

      val Some(result) = route(app, FakeRequest(GET, "/api/search?query=blabla"))

      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must contain("No movies found for your query")
    }
  }

  "The popular movies API" should {

    "be able to show most popular movies" in new WithApplication(injectable) {
      val mockTMDBApi: TMDBApi = app.injector.instanceOf[TMDBApi]
      mockTMDBApi.popular() returns successful(List(matrix, spidy))

      val Some(result) = route(app, FakeRequest(GET, "/api/popular"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("title" -> "Matrix 11")
      contentAsString(result) must */("title" -> "Spider Man")
    }

    "handle empty results" in new WithApplication(injectable) {
      val mockTMDBApi: TMDBApi = app.injector.instanceOf[TMDBApi]
      mockTMDBApi.popular() returns successful(List())

      val Some(result) = route(app, FakeRequest(GET, "/api/popular"))

      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must contain("No movies found for your query")
    }
  }

  "The pricing API" should {

    "be able to compute invoice pricing" in new WithApplication(injectable) {
      val mockTMDBApi: TMDBApi = app.injector.instanceOf[TMDBApi]
      mockTMDBApi.findById(1) returns successful(Some(matrix))

      val Some(result) = route(app, FakeRequest(GET, "/api/pricing?id=1&days=2"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("Matrix 11")
      contentAsString(result) must */("price" -> "80.0 SEK")
    }

    "warn the user if movie is not available" in new WithApplication(injectable) {
      val mockTMDBApi: TMDBApi = app.injector.instanceOf[TMDBApi]
      mockTMDBApi.findById(4) returns successful(None)

      val Some(result) = route(app, FakeRequest(GET, "/api/pricing?id=4"))

      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must contain("Movie 4 not found")
    }
  }

  "The invoice API" should {
    "invoice a rental of several movies" in new WithApplication(injectable) {
      val mockTMDBApi: TMDBApi = app.injector.instanceOf[TMDBApi]
      mockTMDBApi.findById(1) returns successful(Some(matrix))
      mockTMDBApi.findById(2) returns successful(Some(spidy))
      mockTMDBApi.findById(3) returns successful(Some(spidy2))
      mockTMDBApi.findById(4) returns successful(Some(outOfAfrica))

      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]
      mockInvoiceRepository.save(anyListOf[Rental]) answers { i =>
        val rentals = i.asInstanceOf[List[Rental]]
        val invoices = rentals.map(Invoice.fromRental)

        successful(rentals.zip(invoices))
      }

      val Some(result) = route(app, FakeRequest(POST, "/api/invoice").withJsonBody(Json.parse(
        """
          |[{
          |   "id": 1,
          |   "days": 1
          |}, {
          |   "id": 2,
          |   "days": 5
          |}, {
          |   "id": 3,
          |   "days": 2
          |}, {
          |   "id": 4,
          |   "days": 7
          |}]
        """.stripMargin)))

      status(result) must equalTo(OK)
      contentAsString(result) must */("rentals") */("title" -> "Matrix 11")
      contentAsString(result) must */("invoices") */("paid" -> 40)
      contentAsString(result) must */("total" -> "250.0 SEK")

      there was one(mockInvoiceRepository).save(haveSize[List[Rental]](4))
    }

    "error on unknown movies from invoice" in new WithApplication(injectable) {
      val mockTMDBApi: TMDBApi = app.injector.instanceOf[TMDBApi]
      mockTMDBApi.findById(1) returns successful(Some(matrix))
      mockTMDBApi.findById(99) returns successful(None)

      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]

      val Some(result) = route(app, FakeRequest(POST, "/api/invoice").withJsonBody(Json.parse(
        """
          |[{
          |   "id": 1,
          |   "days": 1
          |}, {
          |   "id": 99,
          |   "days": 7
          |}]
        """.stripMargin)))

      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must beEqualTo("Movie 99 not found")

      there was no(mockInvoiceRepository).save(anyListOf[Rental])
    }

    "show request errors" in new WithApplication(injectable) {
      val mockTMDBApi: TMDBApi = app.injector.instanceOf[TMDBApi]
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]

      val Some(result) = route(app, FakeRequest(POST, "/api/invoice").withJsonBody(Json.parse(
        """
          |[{
          |   "movieTitle": "Matrix 11"
          |}]
        """.stripMargin)))

      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must */("error.path.missing")

      there was no(mockTMDBApi).findById(anyLong)
      there was no(mockInvoiceRepository).save(anyListOf[Rental])
    }

    "be able to display previous invoices" in new WithApplication(injectable) {
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]
      mockInvoiceRepository.invoices() returns Future.successful(List(
        Invoice(UUID.fromString("517b0a78-4fa3-48ee-9f48-a51e2f38e972"), 666, REGULAR_RELEASE, 25, Timestamp.from(Instant.parse("1982-12-18T00:00:00Z")))
      ))

      val Some(result) = route(app, FakeRequest(GET, "/api/invoice"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("id" -> "517b0a78-4fa3-48ee-9f48-a51e2f38e972")
      contentAsString(result) must */("movieId" -> 666)
      contentAsString(result) must */("releaseType" -> "REGULAR_RELEASE")
      contentAsString(result) must */("paid" -> 25)
      contentAsString(result) must */("date" -> "1982-12-18T00:00:00.00Z")
    }
  }

  "The returns API" should {

    "be able to complete invoices" in new WithApplication(injectable) {
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]
      mockInvoiceRepository.update(any[Invoice]) answers { i => successful(i.asInstanceOf[Invoice]) }
      mockInvoiceRepository.findByMovieIds(anyListOf[Long]) answers { is =>
        val invoices = is.asInstanceOf[List[Long]].map(i => Invoice(randomUUID(), i, NEW_RELEASE, 0.0))
        successful(invoices)
      }

      val Some(result) = route(app, FakeRequest(POST, s"/api/returns?returnDate=${now().plus(2, DAYS)}").withJsonBody(Json.parse(
        """
          |[606,603]
        """.stripMargin)))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must /("invoices") */("movieId" -> 606)
      contentAsString(result) must /("invoices") */("movieId" -> 603)
      contentAsString(result) must /("invoices") */("extraCharge" -> 40.0)
      contentAsString(result) must /("extraCharge" -> "80.0 SEK")

      there was two(mockInvoiceRepository).update(any[Invoice])
    }

    "not be able to return more movies than invoiced" in new WithApplication(injectable) {
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]
      mockInvoiceRepository.findByMovieIds(anyListOf[Long]) returns Future.successful(List())

      val Some(result) = route(app, FakeRequest(POST, "/api/returns").withJsonBody(Json.parse(
        """
          |[606,603]
        """.stripMargin)))

      status(result) must equalTo(NOT_FOUND)
      contentAsString(result) must beEqualTo("Unable to find the following movies: 606, 603")
    }

    "not be able to return already completed invoices" in new WithApplication(injectable) {
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]
      mockInvoiceRepository.findByMovieIds(anyListOf[Long]) returns Future.successful(List(
        Invoice(randomUUID(), 606, REGULAR_RELEASE, 20.0, returnDate = Some(Timestamp.from(now())), extraCharge = Some(0.0))
      ))

      val Some(result) = route(app, FakeRequest(POST, "/api/returns").withJsonBody(Json.parse(
        """
          |[606]
        """.stripMargin)))

      status(result) must equalTo(CONFLICT)
      contentAsString(result) must beEqualTo("The following movies were already returned: 606")
      there was no(mockInvoiceRepository).update(any[Invoice])
    }

    "not be able to take invalid values" in new WithApplication(injectable) {
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]

      val Some(result) = route(app, FakeRequest(POST, "/api/returns").withJsonBody(Json.parse(
        """
          |{}
        """.stripMargin)))

      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must */("error.expected.jsarray")
      there was no(mockInvoiceRepository).update(any[Invoice])
    }
  }
}
