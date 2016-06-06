import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter

class Filters @Inject() (gzipFilter: GzipFilter,
                         corsFilter: CORSFilter) extends HttpFilters {

  def filters = Seq(gzipFilter, corsFilter)
}