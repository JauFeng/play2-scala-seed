package filters

import play.api.Logger
import play.api.mvc.{Result, RequestHeader, Filter}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
 * LoggingFilter.
 *
 * (Don't forget 'import play.api.libs.concurrent.Execution.Implicits.defaultContext')
 *
 */
class LoggingFilter extends Filter {

  val logger = Logger(this.getClass)

  override def apply(nextFilter: (RequestHeader) => Future[Result])
                    (requestHeader: RequestHeader): Future[Result] = {

    val startTime = System.currentTimeMillis

    nextFilter(requestHeader) map { result =>

      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime

      Logger.info(s"${requestHeader.method} ${requestHeader.uri}" +
        s"took ${requestTime}ms and returned ${result.header.status}")

      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }

}
