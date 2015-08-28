package controllers

import java.util.concurrent.TimeUnit

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import akka.util.Timeout
import akka.pattern.ask

import com.google.inject.Inject
import controllers.CustomAction.AuthenticatedAction

import play.api.Play.current

import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}
import play.api.libs.json.{JsError, Json, JsValue, Format}
import play.api.mvc.{WebSocket, Action, Controller}

import scala.concurrent.{Future, ExecutionContext}

/**
 * Chat room controller.
 *
 * @param sessionCache
 * @param messagesApi
 * @param executionContext
 *
 * @todo issue: `java.nio.channels.ClosedChannelException`
 */
class ChatRoomController @Inject()(@NamedCache("session-cache") sessionCache: CacheApi, val messagesApi: MessagesApi)
                                  (implicit executionContext: ExecutionContext) extends Controller with I18nSupport {
  val logger = Logger(this.getClass)

  /**
   * Entry chat room.
   *
   * @return
   */
  def chatRoom = AuthenticatedAction { implicit request =>
    Ok(views.html.websocket.chat())
  }

  /**
   * ChatRoom broadcast.
   *
   * @return
   */
  def broadcast = WebSocket.tryAccept[JsValue] { request =>
    ChatRooms.broadcast.mapTo[(Iteratee[JsValue, _], Enumerator[JsValue])].map(Right(_))
  }
}

/**
 * Chat Rooms
 */
object ChatRooms {
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  val chatRoomActor: ActorRef = Akka.system.actorOf(Props[ChatRoomActor], "chat-room")

  def broadcast: Future[Any] = chatRoomActor ? "broadcast"
}

/**
 * Chat Room actor.
 */
class ChatRoomActor extends Actor with ActorLogging {
  val logger = Logger(this.getClass)
  implicit val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    super.preStart()
    logger.info("started.")
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    super.postStop()
    logger.info("Stopped.")
  }

  val (enumerator, channel) = Concurrent.broadcast[JsValue]

  override def receive: Receive = {
    case "broadcast" =>
      sender !(Iteratee.foreach[JsValue] {
        e =>
          logger.info(e.toString())
          e.validate[Broadcast].fold(
            invalid = error => {
              channel.push(Json.obj("status" -> "error", "errMsg" -> JsError.toJson(error)))
            },
            valid = broadcast => {
              channel.push(Json.obj("name" -> broadcast.name, "message" -> broadcast.message))
            }
          )
      }, enumerator)
  }
}

case class Broadcast(name: String, message: String)
object Broadcast {
  implicit val format: Format[Broadcast] = Json.format[Broadcast]
}
