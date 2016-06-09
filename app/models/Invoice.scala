package models

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.UUID
import java.util.UUID._

import play.api.libs.json._

case class Invoice(id: UUID,
                   movieId: Long,
                   paid: Double,
                   date: Timestamp = Timestamp.from(Instant.now()),
                   returnDate: Option[Timestamp] = None,
                   extraCharge: Option[Double] = None)

object Invoice {
  implicit val formatTimestamp = timestampFormat

  implicit val invoiceFormat = Json.format[Invoice]

  def fromRental(rental: Rental): Invoice = Invoice(randomUUID(), rental.movie.id, rental.price)
}

object timestampFormat extends Format[Timestamp] {
  val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
  def reads(json: JsValue) = {
    val str = json.as[String]
    JsSuccess(new Timestamp(format.parse(str).getTime))
  }
  def writes(ts: Timestamp) = JsString(format.format(ts))
}