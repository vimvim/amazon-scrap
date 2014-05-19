package actors

import play.api.libs.iteratee.{Concurrent, Enumerator}
import play.api.libs.json._
import play.api.libs.iteratee.Concurrent.Channel

import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import akka.actor.Actor.Receive
import play.api.libs.json.JsString
import play.api.libs.json.JsBoolean

case class CreateSession(userId:String)
case class SessionClosed(userId:String)

case class SessionCommand(userId:String, jsValue:JsValue)
case class CommandResponse(jsValue:JsValue)

case class ChannelMessage(jsValue:JsValue)

case class UserSession(userId:String, enumerator:Enumerator[JsValue], channel:Channel[JsValue], handler:ActorRef)

/**
 * Created by vim on 5/16/14.
 */
class TasksManager extends Actor with ActorLogging {

  var sessions = Map[String,UserSession]()

  var taskId = 0
  var tasks = Map[Int,ActorRef]()

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

      val id = (jsValue \ "id").asInstanceOf[JsNumber].value
      val command = (jsValue \ "command").asInstanceOf[JsString].value
      val data: JsValue = jsValue \ "data"

      command match {
        case "start_scrap_task" =>
          val response = onStartScrapCmd(data)
          sender ! CommandResponse(Json.obj(
            "id" -> JsNumber(id),
            "response" -> response
          ))
      }

    case ScrapTaskStatus(taskId, status, msg) =>

      sessions.foreach((entry) =>  {

        val userId = entry._1
        val userSession = entry._2

        userSession.channel.push(Json.obj(
          "channel" -> JsString(s"task_$taskId"),
          "name" -> JsString("task_status"),
          "data" -> Json.obj(
              "status" -> JsString(status),
              "msg" -> JsString(msg)
          )
        ))
      })
  }

  private def onStartScrapCmd(jsValue:JsValue):JsValue = {

    val url = (jsValue \ "url").asInstanceOf[JsString].value
    taskId = taskId+1

    val taskActor = context.actorOf(Props(classOf[AmazonScrapTask], taskId, url), name = s"task_$taskId")
    tasks += (taskId -> taskActor)

    taskActor ! StartScrap()

    Json.obj(
      "task_id" -> JsString(taskId.toString)
    )
  }
}
