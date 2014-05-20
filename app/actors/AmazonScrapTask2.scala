package actors

import akka.actor.{ActorLogging, Actor}
import org.jsoup.Jsoup

case class ProductParsed(taskId:Int, price:String, availability:String)

/**
 * Created by vim on 5/20/14.
 */
class AmazonScrapTask2(taskId:Int, url:String) extends Actor with ActorLogging {

  override def receive: Receive = {

    case StartScrap() =>
      log.debug(s"Start scrapping: $url")

      this.context.parent ! ScrapTaskStatus(taskId, "started", "Started")

      val doc = Jsoup.connect(url).get()
      val price = doc.select("#priceblock_ourprice").first().text()
      val availability = doc.select("#availability").first().text()

      this.context.parent ! ProductParsed(taskId, price, availability)
  }
}

