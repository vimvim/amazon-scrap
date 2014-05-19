package controllers

import play.api.mvc.{Action, Controller}
import akka.util.Timeout
import scala.concurrent.Future
import scala.concurrent.duration._

import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json.JsValue
import akka.actor.{Props, ActorRef}
import play.api.libs.concurrent.Akka
import actors._
import akka.pattern.ask
import akka.util.Timeout
import actors.UserSession
import actors.CreateSession
import actors.SessionClosed

import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller with Secured {

  implicit val timeout = Timeout(3 seconds)

  var sessionManager:ActorRef = Akka.system.actorOf(Props[TasksManager])

  def index = withAuth {
    implicit request => userId =>
      Ok(views.html.index("Hello Play Framework"))
  }

  def ws = withAuthWS { userid =>

    val sessionFuture = (sessionManager ? CreateSession(userid)).asInstanceOf[Future[UserSession]]
    sessionFuture map {
      userSession =>
        createCommChannels(userSession)
    }
  }

  private def createCommChannels(userSession:UserSession):(Iteratee[JsValue, Unit], Enumerator[JsValue]) = {

    val commandHandler = userCommandHandler(userSession) _
    val iteratee = Iteratee.foreach[JsValue](commandHandler) map {
      _ => sessionManager ! SessionClosed(userSession.userId)
    }

    (iteratee, userSession.enumerator)
  }

  private def userCommandHandler(userSession:UserSession)(jsValue:JsValue):Unit = {

    (userSession.handler ? SessionCommand(jsValue)) onSuccess {

      case CommandResponse(respJsValue) =>
        userSession.channel.push(respJsValue)

      case _ => println("Unexpected response")
    }
  }
}