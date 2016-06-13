package repositories

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import models.ReleaseType._
import models.{Invoice, ReleaseType, Rental}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class InvoiceRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                                  implicit val context: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val Invoices = TableQuery[InvoicesTable]

  implicit val releaseTypeMapper = MappedColumnType.base[ReleaseType, String](
    e => e.toString,
    s => ReleaseType.withName(s)
  )

  private class InvoicesTable(tag: Tag) extends Table[Invoice](tag, "invoice") {
    def id = column[UUID]("id", O.PrimaryKey)
    def movieId = column[Long]("movie_id")
    def releaseType = column[ReleaseType]("release_type")
    def paid = column[Double]("paid")
    def date = column[Timestamp]("date")
    def returnDate = column[Option[Timestamp]]("return_date")
    def extraCharge = column[Option[Double]]("extra_charge")
    def * = (id, movieId, releaseType, paid, date, returnDate, extraCharge) <>((Invoice.apply _).tupled, Invoice.unapply)
  }

  def invoices(): Future[Seq[Invoice]] = db.run(Invoices.result)

  def findByMovieIds(movieIds: List[Long]): Future[Seq[Invoice]] = {
    val query = Invoices.filter(_.movieId inSet movieIds)
    db.run(query.result)
  }

  def save(rentals: List[Rental]): Future[Seq[(Rental, Invoice)]] = {
    val invoices = rentals.map(Invoice.fromRental)

    db.run(Invoices ++= invoices).map(_ => rentals.zip(invoices))
  }

  def update(invoice: Invoice): Future[Invoice] = {
    db.run(Invoices.insertOrUpdate(invoice)).map(_ => invoice)
  }
}