package controllers

import com.google.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.cache.NamedCache
import play.api.cache.CacheApi

import play.filters.csrf.{CSRFCheck, CSRFAddToken}

import scala.concurrent.duration._
import scala.concurrent.{Future, ExecutionContext}

/**
 * Index controller.
 *
 * @param htmlCache cache which named "html-cache"
 * @param messagesApi Message API
 * @param executionContext implicit `play.api.libs.concurrent.Execution.Implicits.defaultContext`
 */
class IndexController @Inject()(@NamedCache("html-cache") htmlCache: CacheApi, val messagesApi: MessagesApi)
                               (implicit executionContext: ExecutionContext) extends Controller with I18nSupport {
  val logger = Logger(this.getClass)

  val loginForm: Form[LoginForm] = Form {
    mapping(
      "name" -> nonEmptyText(minLength = 1, maxLength = 50),
      "password" -> nonEmptyText(minLength = 8, maxLength = 20)
    )(LoginForm.apply)(LoginForm.unapply)
  }

  /**
   * Index page.
   *
   * @return
   */
  def index = CSRFAddToken {
    Action { implicit request =>
      Ok(views.html.index(title = "Index", loginForm = loginForm))
    }
  }
}