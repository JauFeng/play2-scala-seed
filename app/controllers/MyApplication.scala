package controllers

import java.io.File
import javax.inject.Inject

import akka.actor._
import controllers.CustomAction.AuthenticatedAction
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart

import scala.concurrent.{ExecutionContext, Future}

import play.api.Play.current

import play.api.libs.concurrent.{Akka, Promise}
import play.api.libs.iteratee.{Concurrent, Iteratee, Enumerator}

import play.api.{Play, Logger}

import play.api.mvc._
import play.api.i18n._
import play.api.libs.json._

import play.api.libs.ws.{WSAuthScheme, WSResponse, WSRequest, WSClient}
import play.api.libs.oauth.{RequestToken, ConsumerKey, OAuthCalculator}
import play.api.cache.{CacheApi, NamedCache}

import play.api.routing._

/**
 * Combining examples of usually case.
 *
 * @param wSClient
 * @param sessionCache
 * @param messagesApi
 * @param executionContext
 * Play's default execution context is `play.api.libs.concurrent.Execution.defaultContext`.
 */
class MyApplication @Inject()(wSClient: WSClient, @NamedCache("session-cache") sessionCache: CacheApi, val messagesApi: MessagesApi)
                             (implicit executionContext: ExecutionContext) extends Controller with I18nSupport {
  val logger = Logger(this.getClass)

  /**
   * Case: Upload file.
   */
  def upload = Action.async(parse.multipartFormData) { implicit request =>
    request.body.file("file") match {
      case Some(file) =>
        val fileName = file.filename
        val contentType = file.contentType
        file.ref.moveTo(new File(s"/tmp/$fileName.$contentType"))
        Future.successful(Ok("ok"))
      case None =>
        Future.successful(BadRequest("failed"))
    }
  }

  /**
   * Case: Download file.
   */
  def fileDownload = Action async { implicit request =>
    val file = Play.getFile("/README.md")
    Future.successful {
      Ok.sendFile(
        content = file,
        inline = false,
        fileName = f => f.getName
      )
      //      Ok.chunked[String](content = Enumerator[String]("1", "2", "3"))
    }
  }

  /**
   * Example: WebSocket with `using` method.
   *
   * @note
   * synchronous.
   * @return
   */
  def webSocketWithUsing = WebSocket.using[String] { request =>
    val (enumerator, channel) = Concurrent.broadcast[String]
    val in = Iteratee.foreach[String] { s =>
      channel.push(s)
      logger.info(s)
    }
    val out: Enumerator[String] = enumerator
    (in, out)
  }

  /**
   * Example: WebSocket with `tryAccept` method.
   *
   * @note
   * asynchronous.
   * @return
   */
  def webSocketWithTryAccept = WebSocket.tryAccept[String] { request =>
    val (enumerator, channel) = Concurrent.broadcast[String]
    val in = Iteratee.foreach[String] { s =>
      logger.info(s)
      channel.push(s)
    }
    val out: Enumerator[String] = enumerator
    Future successful Right((in, out))
  }

  /**
   * Example: WebSocket with `acceptWithActor` method.
   *
   * @note
   * synchronous
   * @return
   */
  def webSocketWithActor = WebSocket.acceptWithActor[String, String] { request =>
    out =>
      Props[MyWebSocketActor]()
  }

  /**
   * Example: WebSocket with `tryAcceptWithActor` method.
   *
   * @note
   * asynchronous
   * @return
   */
  def webSocketWithTryActor = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
    val f: (RequestHeader) => (ActorRef) => Props = { (req: RequestHeader) =>
      (out: ActorRef) =>
        Props[MyWebSocketActor]()
    }
    Future.successful(Right((actorRef) => f(request)(actorRef)))
  }

  /**
   * Case: Play WS
   *
   * @example
   * {{{
   *                       val wSRequest: WSRequest = wSClient.url(sharingEndpoint)
   *                           .withHeaders(accessTokenKey -> accessTokenValue)
   *                           .withRequestTimeout(rTimeOut)
   *                           .withQueryString(queryParamKey -> city)
   *                           .withAuth(username, password, scheme = WSAuthScheme.BASIC)
   * }}}
   *
   * @see
   * Tow method of Authentication:
   * 1. HTTP basic authentication:
   * {{{
   *                        wSClient.url(url).withAuth(username, password, scheme = WSAuthScheme.BASIC)
   * }}}
   * 2. Signing with OAuth:
   * {{{
   *                        val consumerKey: ConsumerKey = ConsumerKey(key = "52xEY4sGbPlO1FCQRaiAg",
   *                                                                  secret = "KpnmEeDM6XDwS59FDcAmVMQbui8mcceNASj7xFJc5WY")
   *                        val requestToken: RequestToken = RequestToken("16905598-cIPuAsWUI47Fk78guCRTa7QX49G0nOQdwv2SA6Rjz",
   *                                                                  secret = "yEKoKqqOjo4gtSQ6FSsQ9tbxQqQZNq7LB5NGsbyKU")
   *
   *                        val wSRequest: WSRequest = wSClient.url("").sign(OAuthCalculator(consumerKey = consumerKey, token = requestToken))
   * }}}
   * @note  JsValue is strict double-quotation:  `{"city": "San Francisco"}`
   * @return
   */
  def getWeather = Action.async(bodyParser = parse.json) { implicit request =>
    val city: JsResult[String] = request.body.\("city").validate[String]

    val sharingEndpoint = "http://apis.baidu.com/apistore/weatherservice/recentweathers"

    val accessTokenKey = "apikey"
    val accessTokenValue = "fd2a21598534e11d8d107b4fd8b41838"

    val rTimeOut = 10000

    val queryParamKey = "cityname"

    city.fold(
      invalid = error => {
        Future.successful(BadRequest(Json.obj("status" -> "error", "message" -> JsError.toJson(error))))
      },

      valid = city => {
        val wSRequest: WSRequest = wSClient.url(sharingEndpoint)
          .withHeaders(accessTokenKey -> accessTokenValue)
          .withRequestTimeout(rTimeOut)
          .withQueryString(queryParamKey -> city)
        //          .withAuth(username = "", password = "", scheme = WSAuthScheme.BASIC)

        val wSResponse: Future[WSResponse] = wSRequest.get()

        wSResponse.map { response =>
          Ok(response.json)
        }
      }
    )
  }

  /**
   * The weather page.
   * @return
   */
  def weather = AuthenticatedAction { implicit request =>
    Ok(views.html.weather())
  }

  /**
   * Javascript Routing
   *
   * @return
   */
  def javascriptRouter = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("routes")(
        routes.javascript.MyApplication.getWeather
      )).as("text/javascript")
  }
}

class MyWebSocketActor extends Actor with ActorLogging {
  val logger = Logger(this.getClass)

  override def receive = {
    case jsValue: JsValue =>
      logger.info(jsValue.toString())
      sender ! jsValue
    case msg: String =>
      logger.info(msg)
      sender ! s"I received message: $msg"
    case _ =>
      logger.info("Have no idea.")
      sender ! "Have no idea."
  }
}