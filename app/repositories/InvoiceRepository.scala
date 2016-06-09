package repositories

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import models.{Invoice, Rental}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class InvoiceRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                                 implicit val context: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Invoices = TableQuery[InvoicesTable]

  private class InvoicesTable(tag: Tag) extends Table[Invoice](tag, "invoice") {
    def id = column[UUID]("id", O.PrimaryKey)
    def movieId = column[Long]("movie_id")
    def paid = column[Double]("paid")
    def date = column[Timestamp]("date")
    def returnDate = column[Option[Timestamp]]("return_date")
    def extraCharge = column[Option[Double]]("extra_charge")
    def * = (id, movieId, paid, date, returnDate, extraCharge) <>((Invoice.apply _).tupled, Invoice.unapply)
  }

  def invoices(): Future[Seq[Invoice]] = db.run(Invoices.result)

  def save(rentals: List[Rental]): Future[List[(Rental, Invoice)]] = {
    val invoices = rentals.map(Invoice.fromRental)

    db.run(Invoices ++= invoices).map(_ => rentals.zip(invoices))
  }
}