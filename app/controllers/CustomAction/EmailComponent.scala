package controllers.CustomAction

import javax.inject.Inject

import play.api.Play.current
import play.api.mvc.{Action, Controller}

import play.api.libs.mailer._
import org.apache.commons.mail.EmailAttachment

import scala.concurrent.Future


class EmailComponent @Inject()(mailerClient: MailerClient) extends Controller {

  def sendMail = Action.async { implicit request =>
    Future.successful(Ok(email))
  }

  def email: String = {

    val email = Email(
      subject = "Test Email",
      from = "<xxxx@gmail.com>",
      to = Seq("<xxxx@gmail.com>"),
      bodyText = Some("A text message"),
      bodyHtml = Some("<html><body><p>An <b>html</b> message</p></body></html>"),

      attachments = Seq(
        AttachmentFile("README.md", current.getFile("/README.md")),
        AttachmentData(name = "data.txt", data = "data...".getBytes, mimetype = "text/plain", description = Some("test data"), disposition = Some(EmailAttachment.INLINE))
      )
    )

    mailerClient.send(email)
  }
}
