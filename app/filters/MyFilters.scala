package filters

import javax.inject.Inject

import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.cors.CORSFilter
import play.filters.csrf.CSRFFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

/**
 * Play filters.
 *
 * You can configure it in `application.conf` using `play.http.filters`.
 *
 * @param gzipFilter
 * See more: https://www.playframework.com/documentation/2.4.x/GzipEncoding
 * @param securityHeadersFilter
 * See more: https://www.playframework.com/documentation/2.4.x/SecurityHeaders
 * @param cORSFilter
 * See more: https://www.playframework.com/documentation/2.4.x/CorsFilter
 * @deprecated cSRFFilter
 *             (Sometimes global CSRF filtering may not be appropriate, i.e.OpenID 2.0)
 *             See more: https://www.playframework.com/documentation/2.4.x/ScalaCsrf
 *
 **/
class MyFilters @Inject()(gzipFilter: GzipFilter,
                          securityHeadersFilter: SecurityHeadersFilter,
                          cORSFilter: CORSFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(gzipFilter, securityHeadersFilter, cORSFilter)
}