package models

import dal.UserRepository
import org.specs2.matcher.ValueCheck

import play.api.Play
import play.api.test.{FakeApplication, WithApplication, PlaySpecification}

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

/**
 * Test for UserRepository with specs2.
 *
 */
class UserRepoSpec extends PlaySpecification {


  "A UserRepository" should {

    " passed the create, modify, get, delete test " in new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val userRepo = Play.current.injector.instanceOf[UserRepository]

      // create
      val createResult: User = Await.result(userRepo.create(name = "sean", password = "pwd"), Duration.Inf)
      createResult.name.must_===("sean") && createResult.password.must_===("pwd")

      // modify
      val modifyResult = Await.result(userRepo.update(User(Some(1), "test1", "pwd", Some("test1"), Some("test1"), false, Some(10))), Duration.Inf)
      modifyResult must_=== 1

      // get
      val getResult = Await.result(userRepo.findByUserId(1), Duration.Inf)
      getResult.id must beSome(1)

      // delete
      val deleteResult = Await.result(userRepo.delete(4), Duration.Inf)
      deleteResult must_=== 1
    }

  }
}