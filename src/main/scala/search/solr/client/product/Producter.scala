package search.solr.client.product

import search.solr.client.queue.MessageQueue
import search.solr.client.util.Logging

/**
  * Created by soledede on 2016/2/14.
  */
object Producter extends Logging {

  val separator = "*-*"

  /**
    *
    * @param startUpdateTime
    * @param endUpdataTime
    * @param totalNum
    * 2343433212*-*234343211*-*34
    * @return
    */
  def send(startUpdateTime: Long, endUpdataTime: Long, totalNum: Int): Boolean = {
    logInfo(s"sendMessage-startTime:$startUpdateTime-endTime:$endUpdataTime-$totalNum")
    if (MessageQueue().sendMsg(startUpdateTime + separator + endUpdataTime + separator + totalNum)) true
    else false
  }

  /**
    *
    * @param minUpdateTime
    * @param totalNum
    * 234343211*-*34
    * @return
    */
  def send(minUpdateTime: Long, totalNum: Int) = {
    logInfo(s"sendMessage-minUpdateTime:$minUpdateTime-$totalNum")
    if (MessageQueue().sendMsg(minUpdateTime + separator + totalNum)) true
    else false
  }
}
