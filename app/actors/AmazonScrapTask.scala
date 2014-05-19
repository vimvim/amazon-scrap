package actors

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.actor.Actor.Receive

case class StartScrap()

/**
 * Created by vim on 5/16/14.
 */
class AmazonScrapTask(url:String, manager:ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {

    case StartScrap() =>
      log.debug(s"Start scrapping: $url")

  }
}
