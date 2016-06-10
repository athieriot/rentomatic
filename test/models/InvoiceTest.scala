package models

import java.sql.Timestamp
import java.time.Instant.now
import java.time.temporal.ChronoUnit.{DAYS, HOURS, MINUTES}
import java.time.{Instant, LocalDate}
import java.util.UUID
import java.util.UUID.randomUUID

import models.ReleaseType._
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

    "be completable with no extra charge" in {
      val incompleteInvoice = Invoice(randomUUID(), 1, NEW_RELEASE, 40.0)

      val returnDate: Instant = now().plus(1, DAYS)
      incompleteInvoice.complete(Some(returnDate)) should matchA[Invoice]
        .returnDate(Some(Timestamp.from(returnDate)))
        .extraCharge(Some(0.0))
    }

    "be completable with late charge" in {
      val incompleteInvoice = Invoice(randomUUID(), 1, OLD_RELEASE, 30.0)

      val returnDate: Instant = now().plus(7, DAYS)
      incompleteInvoice.complete(Some(returnDate)) should matchA[Invoice]
        .returnDate(Some(Timestamp.from(returnDate)))
        .extraCharge(Some(60.0))
    }

    "take one day as a 24h period for completion" in {
      val incompleteInvoice = Invoice(randomUUID(), 1, REGULAR_RELEASE, 30.0)

      val returnDate: Instant = now().plus(3, DAYS).plus(23, HOURS)
      incompleteInvoice.complete(Some(returnDate)) should matchA[Invoice]
        .returnDate(Some(Timestamp.from(returnDate)))
        .extraCharge(Some(0.0))
    }

    "take one day as a 24h period for late completion" in {
      val incompleteInvoice = Invoice(randomUUID(), 1, REGULAR_RELEASE, 30.0)

      val returnDate: Instant = now().plus(3, DAYS).plus(24, HOURS)
      incompleteInvoice.complete(Some(returnDate)) should matchA[Invoice]
        .returnDate(Some(Timestamp.from(returnDate)))
        .extraCharge(Some(30.0))
    }

    "Should not refund if completed early" in {
      val incompleteInvoice = Invoice(randomUUID(), 1, REGULAR_RELEASE, 30.0)

      val returnDate: Instant = now().plus(2, HOURS)
      incompleteInvoice.complete(Some(returnDate)) should matchA[Invoice]
        .returnDate(Some(Timestamp.from(returnDate)))
        .extraCharge(Some(0.0))
    }

    "return an error when trying to complete twice" in {
      val completedInvoice = Invoice(randomUUID(), 1, NOT_RELEASED, 0, returnDate = Some(Timestamp.from(now())), extraCharge = Some(0))

      completedInvoice.complete(None) must throwA[IllegalStateException](message = "This invoice has already been completed")
    }

    "not be allowed to be completed in the past" in {
      val invoice = Invoice(randomUUID(), 1, NOT_RELEASED, 0)

      invoice.complete(Some(now().minus(2, MINUTES))) must throwA[IllegalArgumentException](message = "Not possible to return a movie in the past")
    }
  }

  "An implicit format" should {

    "be able to read a timestamp from JSON" in {
      timestampFormat.reads(JsString("1982-12-18T00:00:00.00Z")).get must beEqualTo(Timestamp.valueOf("1982-12-18 00:00:00"))
    }

    "be able to read a release type from JSON" in {

      releaseTypeFormat.reads(JsString("NOT_RELEASED")).get must beEqualTo(NOT_RELEASED)
    }
  }
}
