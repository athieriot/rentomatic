package models

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Duration.between
import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.UUID._

import models.ReleaseType.ReleaseType
import play.api.Logger
import play.api.libs.json._

case class Invoice(id: UUID,
                   movieId: Long,
                   releaseType: ReleaseType,
                   paid: Double,
                   date: Timestamp = Timestamp.from(now()),
                   returnDate: Option[Timestamp] = None,
                   extraCharge: Option[Double] = None) {

  def isComplete = returnDate.isDefined

  def complete(returnDate: Option[Instant]): Invoice = this.isComplete match {
    case _ if returnDate.exists(_.isBefore(date.toInstant)) => throw new IllegalArgumentException("Not possible to return a movie in the past")
    case true => throw new IllegalStateException("This invoice has already been completed")
    case false =>
      val returnTimestamp = Timestamp.from(returnDate.getOrElse(now()))
      val totalDays = between(date.toInstant, returnTimestamp.toInstant).toDays
      val totalPrice = Rental.price(releaseType, totalDays.toInt)

      val charge = if (totalPrice - paid < 0) 0 else totalPrice - paid

      Logger.info(s"Invoice $id has been completed after $totalDays days and with an extra charge of $charge SEK")
      copy(
        returnDate  = Some(returnTimestamp),
        extraCharge = Some(charge)
      )
  }
}

object Invoice {
  implicit val formatTimestamp = timestampFormat

  implicit val invoiceFormat = Json.format[Invoice]

  def fromRental(rental: Rental): Invoice = Invoice(randomUUID(), rental.movie.id, rental.movie.releaseType, rental.price)
}

object timestampFormat extends Format[Timestamp] {
  val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
  def reads(json: JsValue) = {
    val str = json.as[String]
    JsSuccess(new Timestamp(format.parse(str).getTime))
  }
  def writes(ts: Timestamp) = JsString(format.format(ts))
}