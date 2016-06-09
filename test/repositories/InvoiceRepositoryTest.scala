package repositories

import java.time.LocalDate

import models.{Invoice, Movie, Rental}
import play.api.test.{PlaySpecification, WithApplication}

import scala.concurrent.Await
import scala.concurrent.duration._

class InvoiceRepositoryTest extends PlaySpecification {

  //TODO: Not convinced by this test. Need a way to inject data in Table + enforce in memory instance.
  "The InvoiceRepository"  should {
    "allow to persist invoices for a customer rental" in new WithApplication {
      val invoiceRepository: InvoiceRepository = app.injector.instanceOf[InvoiceRepository]

      val lucifer = Rental(Movie(666, "Lucider", None), 2)
      val matrix = Rental(Movie(1, "Matrix 11", Some(LocalDate.now().minusYears(6))), 8)

      val newInvoices: List[(Rental, Invoice)] = Await.result(invoiceRepository.save(List(lucifer, matrix)),  10.seconds)

      newInvoices must haveSize(2)
      newInvoices.unzip._1 must contain(matrix, lucifer).exactly

      val previousInvoices: Seq[Invoice] = Await.result(invoiceRepository.invoices(), 10.seconds)

      previousInvoices.toList should beEqualTo(newInvoices.unzip._2)
    }
  }
}
