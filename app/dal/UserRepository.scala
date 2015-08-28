package dal

import com.google.inject.{Singleton, Inject}
import play.api.Logger

import play.api.db.slick.DatabaseConfigProvider

import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

import models.User
import slick.lifted.ProvenShape

import scala.concurrent.{Future, ExecutionContext}

/**
 * A repository for User model.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 *
 * @note It's Singleton.
 *
 */
@Singleton class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val logger = Logger(this.getClass)

  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.

  import dbConfig._

  /** The starting point for all queries on the user table. */
  private val user = TableQuery[UserTable]

  /**
   * Create a user with the given `name`, `password`, `fullName`, `email`, `isAdmin`, `age`.
   *
   * This is an asynchronous operation, it will return a future of the created user, which can be used to obtain the id for that user.
   */
  def create(name: String, password: String, fullName: Option[String] = None, email: Option[String] = None, isAdmin: Boolean = false, age: Option[Int] = None): Future[User] = db.run {
    // We create a projection of just the table columns, since we're not inserting a value for the id column
    (user.map(u => (u.name, u.password, u.fullName, u.email, u.isAdmin, u.age))
      // Now define it to return the id, because we want to know what id was generated for the user
      returning user.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the returned id
      into ((userTuple, id) => User(id, userTuple._1, userTuple._2, userTuple._3, userTuple._4, userTuple._5, userTuple._6))
      // And finally, insert the user into the database
      ) +=(name, password, fullName, email, isAdmin, age)
  }

  /**
   * Update user.
   *
   * @param user User.
   * @return
   */
  def update(user: User): Future[Int] = db.run {
    logger.info(s"SQL statements: ${this.user.filter(_.id === user.id).update(user).statements}")

    this.user.filter(_.id === user.id).update(user)
  }

  /**
   * Delete user.
   *
   * @param id  id of user.
   * @return
   */
  def delete(id: Long): Future[Int] = db.run {
    logger.info(s"SQL statements: ${this.user.filter(_.id ===  id).delete.statements}")

    this.user.filter(_.id ===  id).delete
  }

  /**
   * List all the users in the database.
   */
  def list(): Future[Seq[User]] = db.run {
    logger.info(s"SQL statements: ${this.user.result.statements}")

    this.user.result
  }

  /**
   * Find user by user id.
   *
   * @param id id of User.
   * @return
   */
  def findByUserId(id: Long): Future[User] = db.run {
    logger.info(s"SQL statements: ${this.user.filter(_.id === id).result.head.statements}")

    this.user.filter(_.id === id).result.head
  }

  /**
   * Find user by username.
   *
   * @param userName User's name
   * @return
   */
  def findByUserName(userName: String): Future[Seq[User]] = db.run {
    logger.info(s"SQL statements: ${this.user.filter(_.name === userName).result.statements}")

    this.user.filter(_.name === userName).result
  }
}

/** Here we define the table. It will have a name of User. */
class UserTable(tag: Tag) extends Table[User](tag, "USER") {

  /** The `ID` column, which is the primary key, and auto incremented */
  def id: Rep[Option[Long]] = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)

  /** The `NAME` column */
  def name: Rep[String] = column[String]("NAME")

  /** The `PASSWORD` column */
  def password: Rep[String] = column[String]("PASSWORD")

  /** The `FULL_NAME` column */
  def fullName: Rep[Option[String]] = column[Option[String]]("FULL_NAME")

  /** The `EMAIL` column */
  def email: Rep[Option[String]] = column[Option[String]]("EMAIL")

  /** The `IS_ADMIN` column */
  def isAdmin: Rep[Boolean] = column[Boolean]("IS_ADMIN", O.Default(false))

  /** The `AGE` column */
  def age: Rep[Option[Int]] = column[Option[Int]]("AGE")


  /**
   * This is the tables default "projection".
   *
   * It defines how the columns are converted to and from the User object.
   */
  override def * : ProvenShape[User] = (id, name, password, fullName, email, isAdmin, age) <>((User.apply _).tupled, User.unapply)
}