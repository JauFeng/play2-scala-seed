package controllers

import controllers.CustomAction.AuthenticatedAction
import dal.UserRepository

import models.User

import com.google.inject.Inject

import play.api._
import play.api.cache.CacheApi
import play.api.cache.NamedCache
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json


import play.filters.csrf.{CSRFAddToken, CSRFCheck}

import scala.concurrent.{ExecutionContext, Future}

/**
 * The user form.
 *
 * Generally for forms, you should define separate objects to your models, since forms very often need to present data
 * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
 * that is generated once it's created.
 */
case class UserForm(name: String,
                    password: String,
                    fullName: Option[String],
                    email: Option[String],
                    isAdmin: Boolean,
                    age: Option[Int])

/**
 * User Repository.
 *
 * @param repo  inject the singleton UserRepository.
 * @param messagesApi I18n-support.
 * @param sessionCache  using `session-cache` cache.
 * @param executionContext  using the `play.api.libs.concurrent.Execution.defaultContext`.
 */
class UserController @Inject()(repo: UserRepository, val messagesApi: MessagesApi, @NamedCache("session-cache") sessionCache: CacheApi)(implicit executionContext: ExecutionContext) extends Controller with I18nSupport {
  val logger = Logger(this.getClass)

  /** Home Redirect. */
  val Home = Redirect(routes.LoginController.login())


  /**
   * The mapping for the user form.
   *
   * @example
   * {{{
   *                    val userForm: Form[UserForm] = Form {
          mapping(
            "name" -> nonEmptyText,
            "password" -> number.verifying(min(0), max(140)),
            "company" -> optional(mapping(
              "name" -> nonEmptyText,
              "phone" -> optional(number(1000000, 9999999, true)),
              "email" -> optional(email),
              "country" -> optional(text(0, 100))
            )(CreateCompanyForm.apply)(CreateCompanyForm.unapply))
          )(UserForm.apply)(UserForm.unapply)
        }
   * }}}
   * `text`: maps to scala.String, optionally takes minLength and maxLength.
   * `nonEmptyText`: maps to scala.String, optionally takes minLength and maxLength.
   * `number`: maps to scala.Int, optionally takes min, max, and strict.
   * `longNumber`: maps to scala.Long, optionally takes min, max, and strict.
   * `bigDecimal`: takes precision and scale.
   * `date`, `sqlDate`, `jodaDate`: maps to java.util.Date, java.sql.Date and org.joda.time.DateTime, optionally takes pattern and timeZone.
   * `jodaLocalDate`: maps to org.joda.time.LocalDate, optionally takes pattern.
   * `email`: maps to scala.String, using an email regular expression.
   * `boolean`: maps to scala.Boolean.
   * `checked`: maps to scala.Boolean.
   * `optional`: maps to scala.Option.
   */
  val userForm: Form[UserForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText(8, 20),
      "fullName" -> optional(text(0, 50)),
      "email" -> optional(email),
      "isAdmin" -> boolean,
      "age" -> optional(number(0, 100))
    )(UserForm.apply)(UserForm.unapply)
  }

  /**
   * Entry the create user page.
   *
   */
  def savePage = CSRFAddToken {
    Action { implicit request =>
      Ok(views.html.user.create(userForm))
    }
  }

  /**
   * Save user action.
   *
   */
  def save = CSRFCheck {
    Action.async(bodyParser = parse.urlFormEncoded) { implicit request =>
      userForm.bindFromRequest.fold(
        errorForm => {
          Future.successful {
            render {
              case Accepts.Html() =>
                BadRequest(views.html.user.create(errorForm))
              case Accepts.Json() =>
                BadRequest(errorForm.errorsAsJson)
            }
          }
        },
        createUserForm => {
          repo.create(name = createUserForm.name, password = createUserForm.password, fullName = createUserForm.fullName, email = createUserForm.email, isAdmin = createUserForm.isAdmin, age = createUserForm.age).map { user =>
            render {
              case Accepts.Html() =>
                Redirect(routes.UserController.savePage()).flashing("success" -> "%s has been added.".format(user.name))
              case Accepts.Json() =>
                import scala.concurrent.duration._
                sessionCache.set("user", user, 5.minutes)
                sessionCache.getOrElse[User]("user", 1.minutes)(User(name = "play", password = "pwd"))
                sessionCache.remove("user")

                Ok(Json.parse("{ success : %s has been added }".format(user.name)))
            }
          }
        }
      )
    }
  }

  /**
   * Entry edit user action.
   *
   * @param id id of user.
   * @return
   */
  def editPage(id: Long) = CSRFAddToken {
    Action.async { implicit request =>
      repo.findByUserId(id).map { user =>
        Ok(views.html.user.edit(id, userForm.fill(
          UserForm(user.name, user.password, user.fullName, user.email, user.isAdmin, user.age)
        )))
      }
    }
  }

  /**
   * Edit user action.
   *
   * @return
   */
  def edit(id: Long) = CSRFCheck {
    Action.async { implicit request =>
      userForm.bindFromRequest.fold(
        hasErrors = error => {
          Future.successful(BadRequest(views.html.user.edit(id, error)))
        },
        success = userForm => {
          repo.update(User(Some(id), userForm.name, userForm.password, userForm.fullName, userForm.email, userForm.isAdmin, userForm.age)).map { result =>
            Redirect(routes.UserController.editPage(id)).flashing("success" -> "%s has been edited.".format(userForm.name))
          }
        }
      )
    }
  }

  /**
   * Delete user action.
   *
   * @param id id of user.
   * @return
   */
  def delete(id: Long) = CSRFCheck {
    Action.async { implicit request =>
      repo.delete(id).map { result =>
        Redirect(routes.UserController.savePage()).flashing("success" -> s"$result user has been deleted.")
      }
    }
  }

  /**
   * Get the users.
   *
   * @example
   * 1. Return `None` - `foreach()`:
   * {{{
   *                      future.foreach{ _ => print(_)}
   * }}}
   * 2. Return `Any` - `map()`:
   * {{{
   *                      future.map(x => x%2 == 0)
   * }}}
   * 3. Return `Future[Any]` - `flatMap()`:
   * {{{
   *                      future.flatMap(x => Future { x % 2 == 0 })
   * }}}
   * 4. Return `Future[(Any, Any)]` - `zip();sequence()`:
   * {{{
   *                      val f: Future[(Any, Any)] = future.zip(futureY)
   *                      f map { case (a, b) => ??? }
   *                      future.sequence(Seq(futureX, futureY, futureZ, …)
   * }}}
   * 5. `recover{ NonFatal(e) => y }`:
   * {{{
   *                      future.recover { case NonFatal(e) => y }
   * }}}
   */
  def users = AuthenticatedAction.async { implicit request =>
    repo.list().map { users =>
      render {
        case Accepts.Html() => Ok(views.html.user.list(users))
        case Accepts.Json() => Ok(Json.toJson(users))
      }
    }
  }

  /**
   * A REST endpoint that gets all the user as JSON.
   *
   */
  def getUsers = Action.async { implicit request =>
    repo.list().map { users =>
      Ok(Json.toJson(users))
    }
  }
}
