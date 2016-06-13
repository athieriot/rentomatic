package services

import java.time.LocalDate

import base.WsMockClient
import models.Movie
import play.api.libs.json.Json
import play.api.mvc._
import play.api.routing.sird._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration._

class MovieCatalogueTest extends PlaySpecification with WsMockClient {

  sequential

  "The Movie Catalogue" should {
    "propose a list of popular movies" in {
      withMovieCatalogue {
        case play.api.routing.sird.GET(p"/movie/popular") => Action {
          Results.Ok(Json.parse(
            """
              |{
              | "page": 1,
              | "results": [{
              |   "poster_path": "/inVq3FRqcYIRl2la8iZikYYxFNR.jpg",
              |   "adult": false,
              |   "overview": "Based upon Marvel Comics’ most unconventional anti-hero, DEADPOOL tells the origin story...",
              |   "release_date": "2016-02-09",
              |   "genre_ids": [ 28, 12, 35, 10749 ],
              |   "id": 293660,
              |   "original_title": "Deadpool",
              |   "original_language": "en",
              |   "title": "Deadpool",
              |   "backdrop_path": "/nbIrDhOtUpdD9HKDBRy02a8VhpV.jpg",
              |   "popularity": 82.51684,
              |   "vote_count": 3677,
              |   "video": false,
              |   "vote_average": 7.17
              | }]
              |}
            """.stripMargin))
        }
      } { api =>
        val movies: List[Movie] = Await.result(api.popular(), 10.seconds)

        movies must haveSize(1)
        movies must contain(
          Movie(
            293660,
            "Deadpool",
            Some(LocalDate.parse("2016-02-09")),
            adult = false,
            Some("/inVq3FRqcYIRl2la8iZikYYxFNR.jpg")
          )
        )
      }
    }

    "handle no popular movies" in {
      withMovieCatalogue {
        case play.api.routing.sird.GET(p"/movie/popular") => Action {
          Results.Ok(Json.parse(
            """
              |{
              | "page": 0,
              | "results": []
              |}
            """.stripMargin))
        }
      } { api =>
        val movies: List[Movie] = Await.result(api.popular(), 10.seconds)

        movies must beEmpty
      }
    }

    "be able to search for movies by title" in {
      withMovieCatalogue {
        case play.api.routing.sird.GET(p"/search/movie") => Action {
          Results.Ok(Json.parse(
            """
              |{
              | "page": 1,
              | "results": [{
              |   "poster_path": "/lryNn7sNkvQIg45KwgeKnMxSSRX.jpg",
              |   "adult": false,
              |   "overview": "Everyone's favorite novice wizard, Harry Potter, continues his high-flying adventures at Hogwarts...",
              |   "release_date": "2002-11-13",
              |   "genre_ids": [ 12, 14, 10751 ],
              |   "id": 672,
              |   "original_title": "Harry Potter and the Chamber of Secrets",
              |   "original_language": "en",
              |   "title": "Harry Potter and the Chamber of Secrets",
              |   "backdrop_path": "/avqzwKn89VetTEvAlBePt3Us6Al.jpg",
              |   "popularity": 4.964612,
              |   "vote_count": 2860,
              |   "video": false,
              |   "vote_average": 7.04
              | }]
              |}
            """.stripMargin))
        }
      } { api =>
        val movies: List[Movie] = Await.result(api.search("harry potter"), 10.seconds)

        movies must haveSize(1)
        movies must contain(
          Movie(
            672,
            "Harry Potter and the Chamber of Secrets",
            Some(LocalDate.parse("2002-11-13")),
            adult = false,
            Some("/lryNn7sNkvQIg45KwgeKnMxSSRX.jpg")
          )
        )
      }
    }

    "handle empty search results" in {
      withMovieCatalogue {
        case play.api.routing.sird.GET(p"/search/movie") => Action {
          Results.Ok(Json.parse(
            """
              |{
              | "page": 0,
              | "results": []
              |}
            """.stripMargin))
        }
      } { api =>
        val movies: List[Movie] = Await.result(api.search("Firefly"), 10.seconds)

        movies must beEmpty
      }
    }

    "be able to search movies by id" in {
      withMovieCatalogue {
        case play.api.routing.sird.GET(p"/movie/603") => Action {
          Results.Ok(Json.parse(
            """
              |{
              | "adult": false,
              | "backdrop_path": "/7u3pxc0K1wx32IleAkLv78MKgrw.jpg",
              | "budget": 63000000,
              | "genres": [{
              |   "id": 12,
              |   "name": "Adventure"
              | }],
              | "homepage": "http://www.warnerbros.com/movies/home-entertainment/the-matrix/37313ac7-9229-474d-a423-44b7a6bc1a54.html",
              | "id": 603,
              | "imdb_id": "tt0133093",
              | "original_language": "en",
              | "original_title": "The Matrix",
              | "overview": "Thomas A. Anderson is a man living two lives. By day he is an average computer programmer and by night ...",
              | "popularity": 4.259443,
              | "poster_path": "/lZpWprJqbIFpEV5uoHfoK0KCnTW.jpg",
              | "release_date": "1999-03-30",
              | "revenue": 463517383,
              | "runtime": 136,
              | "status": "Released",
              | "tagline": "Welcome to the Real World.",
              | "title": "The Matrix",
              | "video": false,
              | "vote_average": 7.7,
              | "vote_count": 5537
              |}
            """.stripMargin))
        }
      } { api =>
        val movie: Option[Movie] = Await.result(api.findById(603), 10.seconds)

        movie must beSome(
          Movie(
            603,
            "The Matrix",
            Some(LocalDate.parse("1999-03-30")),
            adult = false,
            Some("/lZpWprJqbIFpEV5uoHfoK0KCnTW.jpg")
          )
        )
      }
    }

    "handle movie not found by id" in {
      withMovieCatalogue {
        case play.api.routing.sird.GET(p"/movie/99") => Action {
          Results.NotFound
        }
      } { api =>
        val movie: Option[Movie] = Await.result(api.findById(99), 10.seconds)

        movie must beNone
      }
    }

    "support empty release dates" in {
      withMovieCatalogue {
        case play.api.routing.sird.GET(p"/movie/313428") => Action {
          Results.Ok(Json.parse(
            """
              |{
              | "id": 313428,
              | "adult": false,
              | "title": "Wolverine and the X-Men: Vol. 5: Revelation",
              | "release_date": ""
              |}
            """.stripMargin))
        }
      } { api =>
        val movie: Option[Movie] = Await.result(api.findById(313428), 10.seconds)

        movie must beSome(
          Movie(
            313428,
            "Wolverine and the X-Men: Vol. 5: Revelation",
            None,
            adult = false,
            None
          )
        )
      }
    }
  }
}
