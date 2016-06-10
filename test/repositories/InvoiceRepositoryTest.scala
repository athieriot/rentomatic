package repositories

import java.time.LocalDate

import models.{Invoice, Movie, Rental}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{PlaySpecification, WithApplication}

import scala.concurrent.Await
import scala.concurrent.duration._

//TODO: Need a way to inject data in Tables and rollback between tests.
class InvoiceRepositoryTest extends PlaySpecification {

  sequential

  private val inMemory = { builder: GuiceApplicationBuilder =>
    builder.configure(Configuration(
      "slick.dbs.default.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.default.db.driver" -> "org.h2.Driver",
      "slick.dbs.default.db.url" -> s"jdbc:h2:mem:${this.getClass.getSimpleName};MODE=PostgreSQL;DATABASE_TO_UPPER=FALSE"
    ))
  }

  "The InvoiceRepository"  should {
    "allow to persist invoices for a customer rental" in new WithApplication(inMemory) {
      val invoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]

      val lucifer = Rental(Movie(666, "Lucifer", None), 2)
      val matrix = Rental(Movie(1, "Matrix 11", Some(LocalDate.now().minusYears(6))), 8)

      val newInvoices: Seq[(Rental, Invoice)] = Await.result(invoiceRepository.save(List(lucifer, matrix)),  10.seconds)

      newInvoices must haveSize(2)
      newInvoices.unzip._1 must contain(matrix, lucifer).exactly

      val previousInvoices: Seq[Invoice] = Await.result(invoiceRepository.invoices(), 10.seconds)

      previousInvoices should beEqualTo(newInvoices.unzip._2)
    }

    "allow to update an invoice" in new WithApplication(inMemory) {
      val invoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]

      val lucifer = Rental(Movie(666, "Lucifer", None), 2)
      val (_, luciferInvoice) = Await.result(invoiceRepository.save(List(lucifer)), 10.seconds).head

      val newInvoice = luciferInvoice.complete(None)
      Await.result(invoiceRepository.update(newInvoice), 10.seconds) must beEqualTo(newInvoice)

      val previousInvoice: Invoice = Await.result(invoiceRepository.invoices(), 10.seconds).head
      previousInvoice must beEqualTo(newInvoice)

    }

    "be able to search invoice by movie ids" in new WithApplication(inMemory) {
      val invoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]

      val lucifer = Rental(Movie(666, "Lucifer", None), 2)
      val matrix = Rental(Movie(1, "Matrix 11", Some(LocalDate.now().minusYears(6))), 8)
      val zootopia = Rental(Movie(2, "Zootopia", None), 8)

      Await.result(invoiceRepository.save(List(lucifer, matrix, zootopia)),  10.seconds) must haveSize(3)

      val searchResult: Seq[Invoice] = Await.result(invoiceRepository.findByMovieIds(List(666, 2)),  10.seconds)

      searchResult must haveSize(2)
      searchResult.map(_.movieId) must contain(666L, 2L).exactly
    }
  }
}
