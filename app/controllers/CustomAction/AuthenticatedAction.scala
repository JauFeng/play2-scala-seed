package controllers.CustomAction

import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

/**
 * Authenticated Action.
 */
object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
  private val logger = Logger(this.getClass)

  val UserKey = "user_name"

  /**
   * Authenticated method.
   *
   * @param f
   * @param g
   * @param request
   * @tparam A
   * @return
   */
  def authenticated[A](f: String => A, g: => A)(implicit request: RequestHeader): A = request.session.get(UserKey) match {
    case Some(userName) => f(userName)
    case None => g
  }

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    authenticated(
      userName => block(new AuthenticatedRequest[A](Some(userName), request)),
      Future.successful(
        Redirect(controllers.routes.LoginController.login())
          .withSession(request.session + ("parent" -> request.uri))
          .flashing("forbidden" -> "non-login")
      )
    )(request)
  }
}

/**
 * Authenticated Request.
 *
 * @param username
 * @param request
 * @tparam A
 */
class AuthenticatedRequest[A](val username: Option[String], request: Request[A]) extends WrappedRequest[A](request)