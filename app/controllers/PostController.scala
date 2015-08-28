package controllers

import java.util.Date

import controllers.CustomAction.AuthenticatedAction
import dal.{UserRepository, PostRepository}

import com.google.inject.Inject
import models.{User, Post}

import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.{Field, Form}
import play.api.data.Forms._
import play.api.libs.json.Json

import play.filters.csrf.{CSRFAddToken, CSRFCheck}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/**
 * The create post form.
 *
 * Generally for forms, you should define separate objects to your models, since forms very often need to present data
 * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
 * that is generated once it's created.
 */
case class PostForm(title: String, content: Option[String], postedAt: Option[Date], authorId: Option[Long])

/**
 * Post controller.
 *
 * @param postRepo
 * @param userRepo
 * @param messagesApi
 * @param ec
 */
class PostController @Inject()(postRepo: PostRepository, userRepo: UserRepository, val messagesApi: MessagesApi)
                              (implicit ec: ExecutionContext) extends Controller with I18nSupport {
  val logger = Logger(this.getClass)

  /** Home Redirect. */
  val Home = Redirect(routes.LoginController.login())

  val postForm: Form[PostForm] = Form {
    mapping(
      "title" -> nonEmptyText,
      "content" -> optional(text),
      "postedAt" -> optional(date),
      "authorId" -> optional(longNumber(strict = true))
    )(PostForm.apply)(PostForm.unapply)
  }

  /**
   * Entry the create post page.
   *
   */
  def savePage = CSRFAddToken {
    Action.async(implicit request => userKeyValue.map(seq => Ok(views.html.post.create(postForm, seq))))
  }

  /**
   * The add post action.
   *
   * This is asynchronous, since we're invoking the asynchronous methods on PersonRepository.
   *
   * CSRFCheck add CSRF check to this POST submission.
   *
   */
  def save = CSRFCheck {
    Action.async { implicit request =>
      postForm.bindFromRequest.fold(
        hasErrors = errorForm => {
          userKeyValue.map(seq => Ok(views.html.post.create(errorForm, seq)))
        },
        success = postForm => {
          val post = Post(title = postForm.title, content = postForm.content, postedAt = postForm.postedAt, authorId = postForm.authorId)
          postRepo.save(post).map { _ =>
            Redirect(routes.PostController.savePage()).flashing("success" -> "%s has been added.".format(postForm.title))
          }
        }
      )
    }
  }

  /**
   * Entry The edit post page action.
   *
   * @param id  id of post.
   * @return
   */
  def editPage(id: Long) = CSRFAddToken {
    Action.async { implicit request =>
      postRepo.findById(id) zip userKeyValue map {
        case (post, seq) =>
          val fillForm = post match {
            case Some(Post(id, title, content, postedAt, authorId)) =>
              postForm.fill(PostForm(title, content, postedAt, authorId))
            case None => postForm
          }
          Ok(views.html.post.edit(id, fillForm, seq)
          )
      }
    }
  }

  /**
   * The edit post action.
   *
   * @return
   */
  def edit(id: Long) = CSRFCheck {
    Action.async { implicit request =>
      postForm.bindFromRequest.fold(
        hasErrors = errorForm => {
          userKeyValue.map(seq => Ok(views.html.post.edit(id, errorForm, seq)))
        },
        success = postForm => {
          val post = Post(id = Some(id), title = postForm.title, content = postForm.content, postedAt = postForm.postedAt, authorId = postForm.authorId)
          postRepo.update(post).map { _ =>
            Redirect(routes.PostController.editPage(post.id.getOrElse(-1L))).flashing("success" -> "%s has been edited.".format(postForm.title))
          }
        }
      )
    }
  }

  /**
   * Delete post action,
   *
   * @param id
   * @return
   */
  def delete(id: Long) = CSRFCheck {
    Action.async { implicit request =>
      postRepo.delete(id).map { result =>
        Redirect(routes.PostController.savePage()).flashing("success" -> s"$result post has been deleted.")
      }
    }
  }

  /**
   * Get the post list as Seq[Post].
   *
   */
  def posts = AuthenticatedAction.async { implicit request =>
    postRepo.listWithForeignDependency().map { posts =>
      Ok(views.html.post.list(posts))
    }
  }

  /**
   * A REST endpoint that gets all the post as JSON.
   *
   */
  def getPosts = Action.async { implicit request =>
    postRepo.list().map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def userKeyValue: Future[Seq[(String, String)]] = {
    userRepo.list().map { users =>
      users.map { user => (user.id.getOrElse(-1L).toString, user.name) }
    }
  }

}
