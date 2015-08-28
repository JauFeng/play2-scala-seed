package filters

import javax.inject.{Inject, Provider}

import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router

import scala.concurrent._


class MyErrorHandler @Inject()(env: Environment,
                               config: Configuration,
                               sourceMapper: OptionalSourceMapper,
                               router: Provider[Router]) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override protected def onBadRequest(request: RequestHeader, message: String): Future[Result] = super.onBadRequest(request, message)

  override protected def onNotFound(request: RequestHeader, message: String): Future[Result] = super.onNotFound(request, message)

  override protected def logServerError(request: RequestHeader, usefulException: UsefulException): Unit = super.logServerError(request, usefulException)

  override protected def onDevServerError(request: RequestHeader, exception: UsefulException): Future[Result] = super.onDevServerError(request, exception)


  override protected def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    Future.successful {
      InternalServerError("A server error occurred: " + exception)
    }
  }

  override protected def onForbidden(request: RequestHeader, message: String): Future[Result] = {
    Future.successful {
      Forbidden("You're not allowed to access this resource.")
    }
  }


  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful {
      InternalServerError("A server error occurred: " + exception.getMessage)
    }
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful {
      Status(statusCode)(s"A client error occurred: $message, with statusCode: $statusCode")
    }
  }
}
