package base

import org.specs2.mock.Mockito
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import repositories.InvoiceRepository
import services.TMDBApi

trait ApiMockApplication { this: Mockito =>

  val injectable = { builder: GuiceApplicationBuilder =>
    builder.overrides(
      bind[TMDBApi].toInstance(mock[TMDBApi]),
      bind[InvoiceRepository].toInstance(mock[InvoiceRepository])
    )
  }
}
