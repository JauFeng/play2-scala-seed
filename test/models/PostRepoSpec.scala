package models

import java.util.Date
import java.util.concurrent.TimeUnit

import dal.PostRepository
import play.api.Play

import play.api.test.{FakeApplication, WithApplication, PlaySpecification}

import scala.concurrent.duration.Duration
import scala.concurrent.{Future, Await}

/**
 * Test for PostRepository with specs2.
 *
 */
class PostRepoSpec extends PlaySpecification {

  " A PostRepository " should {

    " passed the save, update, find, delete test " in new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val postRepo: PostRepository = Play.current.injector.instanceOf[PostRepository]

      // save
      val post = Post(title = "post", content = Some("test"), postedAt = Some(new Date()), authorId = Some(1))
      val saveResult: Int = Await.result(postRepo.save(post), Duration.Inf)
      saveResult must_=== 1

      // find
      val findResult: Option[Post] = Await.result(postRepo.findById(1), Duration.Inf)
      findResult must beSome[Post]

      // update
      val updateResult: Int = Await.result(postRepo.update(post), Duration.Inf)
      updateResult must_=== 0

      // delete
      val deleteResult: Int = Await.result(postRepo.delete(1), Duration.Inf)
      deleteResult must_=== 1
    }

  }

}
