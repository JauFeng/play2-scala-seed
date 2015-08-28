import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.test.WithBrowser

/**
 * Add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class IntegrationSpec extends PlaySpecification {

  "Application" should {

    "work from within a browser" in new WithBrowser(app = FakeApplication(additionalConfiguration = inMemoryDatabase())) {

      browser.goTo("http://localhost:" + port)

      browser.title must contain("Index")
    }
  }
}
