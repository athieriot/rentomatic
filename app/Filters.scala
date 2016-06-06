import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

class Filters @Inject() (gzipFilter: GzipFilter,
                         securityHeadersFilter: SecurityHeadersFilter,
                         corsFilter: CORSFilter) extends HttpFilters {

  def filters = Seq(gzipFilter, securityHeadersFilter, corsFilter)
}