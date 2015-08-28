package models

import java.util.Date

import play.api.libs.json._

/**
 *  Post model.
 */
case class Post(id: Option[Long] = None, title: String, content: Option[String] = None, postedAt: Option[Date] = None, authorId: Option[Long] = None)

object Post {
  implicit val postFormat: Format[Post] = Json.format[Post]
}