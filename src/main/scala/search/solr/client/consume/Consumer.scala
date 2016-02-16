package search.solr.client.consume

import search.solr.client.index.manager.IndexManager
import search.solr.client.queue.MessageQueue
import search.solr.client.queue.impl.KafkaMessageQueue
import search.solr.client.util.Logging
import scala.collection.JavaConversions._

/**
  * Created by soledede on 2016/2/14.
  */
object Consumer extends Logging {
  val indexer = IndexManager()

  def main(args: Array[String]) {
    MessageQueue().start() //start recieve message,default we use kafka
    receive
  }

  def receive(): Unit = {
    while (true) {
      val message = KafkaMessageQueue.kafkaBlockQueue.take()
      val data = indexer.requestData(message)
      //generate xml for data
      val xmlBool = indexer.geneXml(data)
      if (xmlBool != null) {
        if (xmlBool.isInstanceOf[java.util.ArrayList[java.lang.String]]) {
          val delData = xmlBool.asInstanceOf[java.util.ArrayList[java.lang.String]]
          if (indexer.delete(delData)) {
            logInfo("delete index success!")
          } else {
            logError("delete index faield!Ids:")
            delData.foreach(id => logInfo(s"delete faield id:\t${id}"))
          }
        }
        else {
          val indexData = xmlBool.asInstanceOf[java.util.ArrayList[java.util.Map[java.lang.String, Object]]]
          if (indexer.indexData(indexData)) logInfo(" index success!")
          else {
            logError("index faield!Ids:")
            indexData.foreach { doc =>
              logInfo(s"index faield id:\t${doc.get("id")}")
            }
          }
        }
      }
    }
  }
}
