package models

import play.api.libs.json._

/**
 *  User model.
 */
case class User(id: Option[Long] = None, name: String, password: String, fullName: Option[String] = None, email: Option[String] = None, isAdmin: Boolean = false, age: Option[Int] = None)

object User {
  /** User format instead of Reader & Writer for transforming json. */
  implicit val userFormat: Format[User] = Json.format[User]
}