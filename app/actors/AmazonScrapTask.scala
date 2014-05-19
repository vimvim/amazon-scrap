package actors

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.actor.Actor.Receive

case class StartScrap()

case class ScrapTaskStatus(taskId:Int, code:String, msg:String)

/**
 * Created by vim on 5/16/14.
 */
class AmazonScrapTask(taskId:Int, url:String) extends Actor with ActorLogging {

  override def receive: Receive = {

    case StartScrap() =>
      log.debug(s"Start scrapping: $url")

      this.context.parent ! ScrapTaskStatus(taskId, "started", "Started")
  }
}
