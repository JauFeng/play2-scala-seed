package dal

import javax.inject.{Singleton, Inject}

import models.{User, Post}

import java.util.Date

import play.api.Logger

import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig

import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{Future, ExecutionContext}

/**
 * A repository for Post model.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 *
 * @note It's Singleton.
 *
 */
@Singleton
class PostRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val logger = Logger(this.getClass)

  private val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  import dbConfig._

  /** The post table for Table POST. */


  /** Post table query. */
  private val post = TableQuery[PostTable]
  private val user = TableQuery[UserTable]

  /** Save post. */
  def save(post: Post): Future[Int] = db.run {
    logger.info(s"SQL statements: ${(this.post += post).statements}")

    this.post += post
  }

  /** Save posts. */
  def save(posts: Seq[Post]): Future[Option[Int]] = db.run {
    logger.info(s"SQL statements: ${(this.post ++= posts).statements}")

    this.post ++= posts
  }

  /** Update content. */
  def update(post: Post): Future[Int] = db.run {
    logger.info(s"SQL statements: ${this.post.filter(_.id === post.id).update(post).statements}")

    this.post.filter(_.id === post.id).update(post)
  }

  /** Delete content. */
  def delete(id: Long): Future[Int] = db.run {
    logger.info(s"SQL statements: ${post.filter(_.id === id).delete.statements}")

    this.post.filter(_.id === id).delete
  }

  /** Find post by id. */
  def findById(id: Long): Future[Option[Post]] = db.run {
    logger.info(s"SQL statements: ${post.filter(_.id === id).result.statements}")

    this.post.filter(_.id === id).result.headOption
  }

  /** Find post list. */
  def list(): Future[Seq[Post]] = db.run {
    logger.info(s"SQL statements: ${post.result.statements}")

    post.result
  }

  /** Post list with user - join left. */
  def listWithForeignDependency(): Future[Seq[(Post, Option[User])]] = db.run {
    val joinLeftQuery: Query[(PostTable, Rep[Option[UserTable]]), (Post, Option[User]), Seq] =
      for {(p, u) <- post joinLeft user on (_.authorId === _.id)} yield (p, u)

    logger.info(s"SQL statement ${joinLeftQuery.result.statements}")

    joinLeftQuery.result
  }


}

class PostTable(tag: Tag) extends Table[Post](tag, "POST") {

  /** Transformation `java.sql.Date` between `java.util.Date` */
  implicit val utilDate2SqlDate =
    MappedColumnType.base[java.util.Date, java.sql.Date](d => new java.sql.Date(d.getTime), d => new java.util.Date(d.getTime))

  def id: Rep[Option[Long]] = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)

  def title: Rep[String] = column[String]("TITLE", O.Default(""))

  def content: Rep[Option[String]] = column[Option[String]]("CONTENT")

  def postedAt: Rep[Option[Date]] = column[Option[Date]]("POSTED_AT")

  def authorId: Rep[Option[Long]] = column[Option[Long]]("AUTHOR_ID")

  override def * : ProvenShape[Post] = (id, title, content, postedAt, authorId) <>((Post.apply _).tupled, Post.unapply)
}