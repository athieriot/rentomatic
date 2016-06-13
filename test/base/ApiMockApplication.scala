package base

import org.specs2.mock.Mockito
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.InvoiceRepository
import services.MovieCatalogue

trait ApiMockApplication { this: Mockito =>

  val injectable = { builder: GuiceApplicationBuilder =>
    builder.overrides(
      bind[MovieCatalogue].toInstance(mock[MovieCatalogue]),
      bind[InvoiceRepository].toInstance(mock[InvoiceRepository])
    )
  }
}
