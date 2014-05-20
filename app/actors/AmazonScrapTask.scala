package actors

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.actor.Actor.Receive
import org.jsoup.Jsoup

case class StartScrap()

case class ScrapTaskStatus(taskId:Int, code:String, msg:String)
case class ProductLinkParsed(taskId:Int, url:String, name:String)

/**
 * Created by vim on 5/16/14.
 */
class AmazonScrapTask(taskId:Int, query:String) extends Actor with ActorLogging {

  override def receive: Receive = {

    case StartScrap() =>
      log.debug(s"Start scrapping: $query")

      this.context.parent ! ScrapTaskStatus(taskId, "started", "Started")

      val doc = Jsoup.connect(s"http://www.amazon.com/s/field-keywords=$query").get()
      val products = doc.select("#atfResults > div")

      val productsItr = products.iterator()
      while (productsItr.hasNext) {

        val product = productsItr.next()

        val productLink = product.select("h3.newaps > a").first()
        val url = productLink.attr("href")
        val name = productLink.text()

        this.context.parent ! ProductLinkParsed(taskId, url, name)
      }

      this.context.parent ! ScrapTaskStatus(taskId, "completed", "Completed")
  }
}
