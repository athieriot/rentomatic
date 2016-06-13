package controllers

import java.sql.{SQLException, Timestamp}
import java.time.temporal.ChronoUnit._
import java.time.{Instant, LocalDate}
import java.util.UUID

import base.ApiMockApplication
import models.ReleaseType.{apply => _, _}
import models.{Invoice, Movie, Rental}
import org.specs2.matcher.JsonMatchers
import org.specs2.mock.Mockito
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.test.{FakeRequest, PlaySpecification, WithApplication, WithServer}
import repositories.InvoiceRepository
import services.MovieCatalogue

import scala.concurrent.Future
import scala.concurrent.Future._

class RentalApiTest extends PlaySpecification with JsonMatchers with Mockito with ApiMockApplication {

  sequential

  private val matrix: Movie = Movie(1, "Matrix 11", Some(LocalDate.now().minus(6, DAYS)))
  private val spidy: Movie = Movie(2, "Spider Man", Some(LocalDate.now().minus(2, YEARS)))
  private val spidy2: Movie = Movie(3, "Spider Man 2", Some(LocalDate.now().minus(2, YEARS)))
  private val outOfAfrica: Movie = Movie(4, "Out of Africa", Some(LocalDate.now().minus(20, YEARS)))

  "The pricing API" should {

    "be able to compute invoice pricing" in new WithApplication(injectable) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      mockMovieCatalogue.findById(1) returns successful(Some(matrix))

      val Some(result) = route(app, FakeRequest(GET, "/api/rental/pricing?id=1&days=2"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("Matrix 11")
      contentAsString(result) must */("price" -> "80.0 SEK")
    }

    "warn the user if movie is not available" in new WithApplication(injectable) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      mockMovieCatalogue.findById(4) returns successful(None)

      val Some(result) = route(app, FakeRequest(GET, "/api/rental/pricing?id=4"))

      status(result) must equalTo(NOT_FOUND)
      contentType(result) must beSome("application/json")
      contentAsString(result) must beEqualTo("\"Movie 4 not found\"")
    }
  }

  "The invoice API" should {
    "invoice a rental of several movies" in new WithApplication(injectable) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      mockMovieCatalogue.findById(1) returns successful(Some(matrix))
      mockMovieCatalogue.findById(2) returns successful(Some(spidy))
      mockMovieCatalogue.findById(3) returns successful(Some(spidy2))
      mockMovieCatalogue.findById(4) returns successful(Some(outOfAfrica))

      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]
      mockInvoiceRepository.save(anyListOf[Rental]) answers { i =>
        val rentals = i.asInstanceOf[List[Rental]]
        val invoices = rentals.map(Invoice.fromRental)

        successful(rentals.zip(invoices))
      }

      val Some(result) = route(app, FakeRequest(POST, "/api/rental/invoice").withJsonBody(Json.parse(
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

      status(result) must equalTo(CREATED)
      contentAsString(result) must */("rentals") */("title" -> "Matrix 11")
      contentAsString(result) must */("invoices") */("paid" -> 40)
      contentAsString(result) must */("total" -> "250.0 SEK")

      there was one(mockInvoiceRepository).save(haveSize[List[Rental]](4))
    }

    "error on unknown movies from invoice" in new WithApplication(injectable) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      mockMovieCatalogue.findById(1) returns successful(Some(matrix))
      mockMovieCatalogue.findById(99) returns successful(None)

      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]

      val Some(result) = route(app, FakeRequest(POST, "/api/rental/invoice").withJsonBody(Json.parse(
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
      contentType(result) must beSome("application/json")
      contentAsString(result) must beEqualTo("\"Movie 99 not found\"")

      there was no(mockInvoiceRepository).save(anyListOf[Rental])
    }

    "handle error as json content" in new WithServer(injectable.apply(GuiceApplicationBuilder()).build()) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      mockMovieCatalogue.findById(1) returns successful(Some(matrix))

      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]
      mockInvoiceRepository.save(anyListOf[Rental]) returns failed(new SQLException("That won't happen"))

      val ws = app.injector.instanceOf[WSClient]

      val response = await(ws.url("http://localhost:19001/api/rental/invoice")
        .withHeaders("Content-Type" -> "application/json")
        .post("[{\"id\": 1, \"days\": 1}]")
      )

      response.status must equalTo(INTERNAL_SERVER_ERROR)
      response.body must beEqualTo("\"A server error occurred: That won't happen\"")
    }

    "show request errors" in new WithApplication(injectable) {
      val mockMovieCatalogue: MovieCatalogue = app.injector.instanceOf[MovieCatalogue]
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]

      val Some(result) = route(app, FakeRequest(POST, "/api/rental/invoice").withJsonBody(Json.parse(
        """
          |[{
          |   "movieTitle": "Matrix 11"
          |}]
        """.stripMargin)))

      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must */("error.path.missing")

      there was no(mockMovieCatalogue).findById(anyLong)
      there was no(mockInvoiceRepository).save(anyListOf[Rental])
    }

    "be able to display previous invoices" in new WithApplication(injectable) {
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]
      mockInvoiceRepository.invoices() returns Future.successful(List(
        Invoice(UUID.fromString("517b0a78-4fa3-48ee-9f48-a51e2f38e972"), 666, REGULAR_RELEASE, 25, Timestamp.from(Instant.parse("1982-12-18T00:00:00Z")))
      ))

      val Some(result) = route(app, FakeRequest(GET, "/api/rental/invoice"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must */("id" -> "517b0a78-4fa3-48ee-9f48-a51e2f38e972")
      contentAsString(result) must */("movieId" -> 666)
      contentAsString(result) must */("releaseType" -> "REGULAR_RELEASE")
      contentAsString(result) must */("paid" -> 25)
      contentAsString(result) must */("date" -> "1982-12-18T00:00:00.00Z")
    }
  }
}
