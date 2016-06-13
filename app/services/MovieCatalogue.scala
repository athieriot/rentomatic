package services

import javax.inject.Inject

import models.Movie
import play.api.Configuration
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class MovieCatalogue @Inject()(ws: WSClient,
                               config: Configuration)(implicit val context: ExecutionContext) {

  private val api_key: String = config.getString("tmdb.api.key").get
  private val base_url: String = config.getString("tmdb.base.url").get

  private val nullifyEmptyDate = (__ \ 'release_date).json.update(
    of[JsString].map{ case JsString(value) => if (value.isEmpty) JsNull else JsString(value) }
  )

  private val cleanMovies = (__ \ 'results).json.pick(
    of[JsArray].map{ case JsArray(arr) => JsArray(arr.map(_.transform(nullifyEmptyDate).get)) }
  )

  def findById(id: Long): Future[Option[Movie]] = {
    get(s"/movie/$id").map { response =>

      if (response.status == 404) None
      else response.json.transform(nullifyEmptyDate).map(_.as[Movie]).asOpt
    }
  }

  def search(query: String): Future[List[Movie]] = {
    get("/search/movie", "query" -> query).map { response =>

      response.json.transform(cleanMovies).map(_.as[List[Movie]]).get
    }
  }

  def popular(): Future[List[Movie]] = {
    get("/movie/popular").map { response =>

      response.json.transform(cleanMovies).map(_.as[List[Movie]]).get
    }
  }

  private def get(path: String, queryStrings: (String, String)*): Future[WSResponse] = {
    val request = ws.url(s"$base_url$path")
      .withHeaders("Accept" -> "application/json")
      .withQueryString("api_key" -> api_key)

    queryStrings
      .foldLeft(request)((r, qs) => r.withQueryString(qs))
      .get()
  }
}
