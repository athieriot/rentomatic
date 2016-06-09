package models

import java.sql.Timestamp
import java.time.LocalDate
import java.util.UUID

import org.specs2.matcher.MatcherMacros
import play.api.libs.json.JsString
import play.api.test.PlaySpecification

class InvoiceTest extends PlaySpecification with MatcherMacros {

  "An Invoice" should {
    "be created from a Rental" in {
      val invoice = Invoice.fromRental(Rental(Movie(666, "Lucifer", Some(LocalDate.now())), 2))

      invoice must matchA[Invoice]
        .id(haveClass[UUID])
        .movieId(666)
        .paid(80)
        .date(haveClass[Timestamp])
        .returnDate(beNone)
        .extraCharge(beNone)
    }
  }

  "A timestamp format" should {

    "be able to read a timestamp from JSON" in {
      timestampFormat.reads(JsString("1982-12-18T00:00:00.00Z")).get must beEqualTo(Timestamp.valueOf("1982-12-18 00:00:00"))
    }
  }
}
