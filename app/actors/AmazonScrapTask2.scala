package actors

import akka.actor.{ActorLogging, Actor}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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
      try{

        val price = getTextSafe(doc, "#priceblock_ourprice", "NA")
        val availability = getTextSafe(doc, "#availability")

        this.context.parent ! ProductParsed(taskId, price, availability)

      } catch {
        case e: Exception => println(s"Exception during processing $url : $e");
      }

      this.context.parent ! ScrapTaskStatus(taskId, "completed", "Completed")
  }

  private def getTextSafe(doc:Document, selector:String, default:String = "") = {
    val elements = doc.select(selector)
    if (elements.size()>0) {
      elements.first().text()
    } else {
      default
    }
  }
}

