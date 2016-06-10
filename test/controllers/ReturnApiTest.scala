package controllers

import java.sql.Timestamp
import java.time.Instant.now
import java.time.temporal.ChronoUnit._
import java.util.UUID.randomUUID

import base.ApiMockApplication
import models.Invoice
import models.ReleaseType.{NEW_RELEASE, REGULAR_RELEASE}
import org.specs2.matcher.JsonMatchers
import org.specs2.mock._
import play.api.libs.json.Json
import play.api.test.{WithApplication, _}
import repositories.InvoiceRepository

import scala.concurrent.Future
import scala.concurrent.Future.successful


class ReturnApiTest extends PlaySpecification with JsonMatchers with Mockito with ApiMockApplication {

  sequential

  "The returns API" should {

    "be able to complete invoices" in new WithApplication(injectable) {
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]
      mockInvoiceRepository.update(any[Invoice]) answers { i => successful(i.asInstanceOf[Invoice]) }
      mockInvoiceRepository.findByMovieIds(anyListOf[Long]) answers { is =>
        val invoices = is.asInstanceOf[List[Long]].map(i => Invoice(randomUUID(), i, NEW_RELEASE, 0.0))
        successful(invoices)
      }

      val Some(result) = route(app, FakeRequest(POST, s"/api/rental/returns?returnDate=${now().plus(2, DAYS)}").withJsonBody(Json.parse(
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

      val Some(result) = route(app, FakeRequest(POST, "/api/rental/returns").withJsonBody(Json.parse(
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

      val Some(result) = route(app, FakeRequest(POST, "/api/rental/returns").withJsonBody(Json.parse(
        """
          |[606]
        """.stripMargin)))

      status(result) must equalTo(CONFLICT)
      contentAsString(result) must beEqualTo("The following movies were already returned: 606")
      there was no(mockInvoiceRepository).update(any[Invoice])
    }

    "not be able to take invalid values" in new WithApplication(injectable) {
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]

      val Some(result) = route(app, FakeRequest(POST, "/api/rental/returns").withJsonBody(Json.parse(
        """
          |{}
        """.stripMargin)))

      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must */("error.expected.jsarray")
      there was no(mockInvoiceRepository).update(any[Invoice])
    }

    "reward rental with bonus points" in new WithApplication(injectable) {
      val mockInvoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]
      mockInvoiceRepository.invoices() returns Future.successful(List(
        Invoice(randomUUID(), 666, REGULAR_RELEASE, 0.0),
        Invoice(randomUUID(), 667, REGULAR_RELEASE, 25.0),
        Invoice(randomUUID(), 668, NEW_RELEASE, 30.0),
        Invoice(randomUUID(), 669, NEW_RELEASE, 250.0)
      ))

      val Some(result) = route(app, FakeRequest(GET, "/api/profile/bonus"))

      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsString(result) must /("points" -> 6)
    }
  }
}
