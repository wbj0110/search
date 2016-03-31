package search.solr.client.keyword

import java.util
import java.util.Calendar
import scala.collection.JavaConversions._

import search.solr.client.cache.KVCache

/**
  * Created by soledede on 2016/3/31.
  */
object HotSearch {

  val WEEK_PREFIX = "week_"
  val cache = KVCache()
  val c = Calendar.getInstance()

  val SEPARATOR = "_"

  val preffixCaculate = s"$WEEK_PREFIX${weekOfMonth}$SEPARATOR"

  val suffixKeys = "*"


  var hotKeywords: java.util.List[String] = null


  def recordAndStatictisKeywords(stringToObject: util.Map[String, Object]): Unit = {
    cache.incrby(s"$preffixCaculate${stringToObject.get("keyWords")}")
    setToLocalCache(caculateHotKeyWords)
  }

  private def caculateHotKeyWords(): java.util.List[String] = {
    val currentWeekKeys = cache.keys(preffixCaculate + suffixKeys)
    val hotKeyWords = currentWeekKeys.map { k =>
      (cache.get(k), k.substring(k.indexOf(preffixCaculate) + 7)) //(keyword,count)
    }.filter(_._1>0).sortBy(_._1).map(_._2).take(10)
    hotKeyWords
  }


  private def setToLocalCache(caculateHotKeyWords: util.List[String]): Unit = {
    hotKeywords = caculateHotKeyWords
  }

  private def weekOfMonth() = {
    c.setTimeInMillis(System.currentTimeMillis())
    c.get(Calendar.WEEK_OF_MONTH)
  }

  def main(args: Array[String]) {
    testCalendar
  }

  def testCalendar() = {

    println(c.getTime)
    println(c.get(Calendar.WEEK_OF_MONTH))
  }

}
