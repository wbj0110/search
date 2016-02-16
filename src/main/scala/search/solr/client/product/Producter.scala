package search.solr.client.product

import search.solr.client.queue.MessageQueue
import search.solr.client.util.Logging
import scala.collection.JavaConversions._

/**
  * Created by soledede on 2016/2/14.
  */
object Producter extends Logging {

  val separator = "-"
  val DELETE = "delete"

  /**
    *
    * @param startUpdateTime
    * @param endUpdataTime
    * @param totalNum
    * eg: 2343433212*-*234343211*-*34
    * @return
    */
  def send(startUpdateTime: Long, endUpdataTime: Long, totalNum: Int): Boolean = {
    logInfo(s"sendMessage-startTime:$startUpdateTime-endTime:$endUpdataTime-totalNum:$totalNum")
    if (MessageQueue().sendMsg(startUpdateTime + separator + endUpdataTime + separator + totalNum)) true
    else false
  }

  /**
    *
    * @param minUpdateTime
    * @param totalNum
    * eg:234343211*-*34
    * @return
    */
  def send(minUpdateTime: Long, totalNum: Int) = {
    logInfo(s"sendMessage-minUpdateTime:$minUpdateTime-totalNum:$totalNum")
    if (MessageQueue().sendMsg(minUpdateTime + separator + totalNum)) true
    else false
  }

  /**
    *
    * @param id
    * delete single id
    * eg: delete-124343455
    */
  def delete(id: String) = {
    logInfo(s"deleteMessage-id:$id")
    if (MessageQueue().sendMsg(DELETE + separator + id)) true
    else false
  }

  /**
    *
    * @param ids
    * delete multiple ids
    * eg:delete-132423-3465453-235345
    */
  def delete(ids: java.util.List[String]) = {
    val idMsg = new StringBuilder()
    if (ids != null && ids.size() > 0) {
      ids.foreach(idMsg.append(_).append(separator))
      idMsg.deleteCharAt(idMsg.length - 1)
      logInfo(s"deleteMessage-id:${idMsg.toString()}")
      if (MessageQueue().sendMsg(DELETE + separator + idMsg.toString())) true
      else false
    } else false
  }
}
