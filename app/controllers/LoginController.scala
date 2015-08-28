package controllers

import javax.inject.Inject

import dal.UserRepository

import models.User

import play.api.{Play, Logger}
import play.api.data.Form
import play.api.data.Forms._

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.cache.CacheApi
import play.api.cache.NamedCache
import play.filters.csrf.{CSRFCheck, CSRFAddToken}

import scala.concurrent.{Future, ExecutionContext}

/**
 * Login Controller.
 *
 * @param userRepo
 * @param userCache
 * @param messagesApi
 * @param ec
 */
class LoginController @Inject()(userRepo: UserRepository, @NamedCache("user-cache") userCache: CacheApi, val messagesApi: MessagesApi)
                               (implicit ec: ExecutionContext) extends Controller with I18nSupport {
  val logger = Logger(this.getClass)

  val loginForm: Form[LoginForm] = Form {
    mapping(
      "name" -> nonEmptyText(minLength = 1, maxLength = 50),
      "password" -> nonEmptyText(minLength = 8, maxLength = 20)
    )(LoginForm.apply)(LoginForm.unapply)
  }

  /**
   * Home page.
   */
  val Home = Redirect(routes.LoginController.login())

  /**
   * Login page.
   *
   * @return
   */
  def login = CSRFAddToken {
    Action { implicit request =>
      Ok(views.html.login.login(loginForm))
    }
  }

  /**
   * Authentication.
   *
   * @return
   */
  def authenticate = CSRFCheck {
    Action.async { implicit request =>
      loginForm.bindFromRequest.fold(
        hasErrors = errorForm => {
          Future.successful(BadRequest(views.html.login.login(errorForm)))
        },
        success = successForm => {
          userRepo findByUserName successForm.name map {
            case Nil =>
              BadRequest(views.html.login.login(loginForm.withGlobalError(message = messagesApi("auth.unknown", successForm.name))))
            case users: Seq[User] =>
              if (users.head.password == successForm.password)
                Redirect(routes.IndexController.index()).addingToSession(
                  "user_name" -> users.head.name,
                  "is_admin" -> users.head.isAdmin.toString,
                  "user_fullName" -> users.head.fullName.getOrElse(""))
              else
                BadRequest(views.html.login.login(
                  loginForm.withGlobalError(message = messagesApi("password.unknown", successForm.password)))
                )
          }
        }
      )
    }
  }

  /**
   * Logout.
   *
   * @return
   */
  def logout = Action { implicit request =>
    Redirect(routes.IndexController.index()).removingFromSession("user_name", "is_admin", "user_fullName")
  }


}

case class LoginForm(name: String, password: String)