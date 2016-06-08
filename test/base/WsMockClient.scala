package base

import play.api.Configuration
import play.api.mvc.{Handler, RequestHeader}
import play.api.test.WsTestClient
import play.core.server.Server
import services.TMDBApi

import scala.concurrent.ExecutionContext.Implicits

trait WsMockClient {

  def withTMDBApi[T](routes: PartialFunction[RequestHeader, Handler])(block: TMDBApi => T): T = {

    Server.withRouter()(routes) { implicit port =>

      WsTestClient.withClient { client =>

        val configuration = Configuration(
          "tmdb.base.url" -> "",
          "tmdb.api.key" -> "asdf54asd5f4"
        )

        block(new TMDBApi(client, configuration)(Implicits.global))
      }
    }
  }
}
