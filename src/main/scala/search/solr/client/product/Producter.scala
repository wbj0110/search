package search.solr.client.product

import search.solr.client.config.Configuration
import search.solr.client.queue.MessageQueue
import search.solr.client.util.Logging
import scala.collection.JavaConversions._

/**
  * Created by soledede on 2016/2/14.
  */
object Producter extends Logging with Configuration {

  val separator = "-"
  val DELETE = "delete"


  def send(startUpdateTime: Long, endUpdataTime: Long, totalNum: Int): Boolean = {
    send(collection, startUpdateTime, endUpdataTime, totalNum)
  }

  def send(minUpdateTime: Long, totalNum: Int): Boolean = {
    send(collection, minUpdateTime, totalNum)
  }


  def delete(id: String): Boolean = {
    delete(collection, id)
  }

  def delete(ids: java.util.List[String]): Boolean = {
    delete(collection, ids)
  }

  /**
    * @param collection
    * @param startUpdateTime
    * @param endUpdataTime
    * @param totalNum
    * eg: mergescloud-2343433212-234343211-34
    *     mergescloud-1456329600-1456477928-1
    *     mergescloud-null-null-740670
    *     mergescloud-null-null-20
    *     screencloud-null-null-25468
    *     screencloud-null-null-40
    * @return
    */
  def send(collection: String, startUpdateTime: Long, endUpdataTime: Long, totalNum: Int): Boolean = {
    logInfo(s"sendMessage-collection:$collection-startTime:$startUpdateTime-endTime:$endUpdataTime-totalNum:$totalNum")
    if (MessageQueue().sendMsg(collection + separator + startUpdateTime + separator + endUpdataTime + separator + totalNum)) true
    else false
  }

  /**
    * @param collection
    * @param minUpdateTime
    * @param totalNum
    * eg:mergescloud-234343211-34
    *    screencloud-234343211-34
    *    mergescloud-1456329600-10
    *    mergescloud-1456219967513-297840
    *    mergescloud-1455960816240-715520
    *    screencloud-1456329600-10
    * @return
    */
  def send(collection: String, minUpdateTime: Long, totalNum: Int) = {
    logInfo(s"sendMessage-collection:$collection-minUpdateTime:$minUpdateTime-totalNum:$totalNum")
    if (MessageQueue().sendMsg(collection + separator + minUpdateTime + separator + totalNum)) true
    else false
  }


  /**
    * @param collection
    * @param id
    * delete single id
    * eg: mergescloud-delete-100429
    *     screencloud-delete-1003484_t87_s
    */
  def delete(collection: String, id: String) = {
    logInfo(s"deleteMessage-collection:$collection-id:$id")
    if (MessageQueue().sendMsg(collection + separator + DELETE + separator + id)) true
    else false
  }

  /**
    * @param collection
    * @param ids
    * delete multiple ids
    * eg:mergescloud-delete-109432-1003435-2562234
    * screencloud-delete-109432-1003435-2562234
    */
  def delete(collection: String, ids: java.util.List[String]) = {
    val idMsg = new StringBuilder()
    if (ids != null && ids.size() > 0) {
      ids.foreach(idMsg.append(_).append(separator))
      idMsg.deleteCharAt(idMsg.length - 1)
      logInfo(s"deleteMessage--collection:$collection-id:${idMsg.toString()}")
      if (MessageQueue().sendMsg(collection + separator + DELETE + separator + idMsg.toString())) true
      else false
    } else false
  }


  /**
    *
    * this is generate  inteface for product message,whenever you want to expand your function
    * @param msg
    * @return
    * eg: test-234-3423-445
    */
  def sendMsg(msg: String): Boolean = {
    logInfo(s"customSendMessage-message:$msg")
    if (MessageQueue().sendMsg(msg)) true
    else false
  }
}
