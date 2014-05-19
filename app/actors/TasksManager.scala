package actors

import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Concurrent.Channel

import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.actor.Actor.Receive

case class CreateSession(userId:String)
case class SessionClosed(userId:String)

case class SessionCommand(userId:String, jsValue:JsValue)
case class CommandResponse(jsValue:JsValue)

case class UserSession(userId:String, enumerator:Enumerator[JsValue], channel:Channel[JsValue], handler:ActorRef)

/**
 * Created by vim on 5/16/14.
 */
class TasksManager extends Actor with ActorLogging {

  var sessions = Map[String,UserSession]()

  override def receive: Receive = {

    case CreateSession(userId) =>

      log.debug(s"Open session for: $userId")

      val session = sessions.getOrElse(userId, {

        val broadcast: (Enumerator[JsValue], Channel[JsValue]) = Concurrent.broadcast[JsValue]
        val session = UserSession(userId, broadcast._1, broadcast._2, self)

        sessions += (userId -> session)

        session
      })

      sender ! session

    case SessionClosed(userId) =>
      log.debug(s"Session closed for: $userId")

    case SessionCommand(userId, jsValue) =>
      log.debug(s"Session command: $userId $jsValue")



  }
}
