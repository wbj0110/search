package search.solr.client.cache


import search.solr.client.config.Configuration

import scala.collection.JavaConversions._
import scala.collection.mutable.StringBuilder

/**
  * Created by soledede on 2016/4/16.
  */
private[search] object SearchCache extends Configuration {
  val cache = KVCache("jRedis")
  val separator = "&_&"

  def getCategoryIdsByKeyWordsCache(keyWords: java.lang.String, cityId: java.lang.Integer, filters: java.util.Map[java.lang.String, java.lang.String]): java.util.List[Integer] = {
    val stringBuilder = categoryIdsByKeyWordsCache(keyWords, cityId, filters)
    if (stringBuilder != null && !stringBuilder.isEmpty)
      cache.getObj[java.util.List[Integer]](stringBuilder.toString())
    else null
  }

  def putCategoryIdsByKeyWordsCache(keyWords: java.lang.String, cityId: java.lang.Integer, filters: java.util.Map[java.lang.String, java.lang.String], catList: java.util.List[Integer]): Unit = {
    val stringBuilder = categoryIdsByKeyWordsCache(keyWords, cityId, filters)
    if (stringBuilder != null && !stringBuilder.isEmpty && catList != null && catList.size() > 0)
      cache.put(stringBuilder.toString.trim, catList, cacheTime)
  }

  private def categoryIdsByKeyWordsCache(keyWords: java.lang.String, cityId: java.lang.Integer, filters: java.util.Map[java.lang.String, java.lang.String]): StringBuilder = {
    val stringBuilder = new StringBuilder
    if (keyWords != null && !keyWords.trim.equalsIgnoreCase(""))
      stringBuilder.append(keyWords.trim).append(separator)
    if (keyWords != null)
      stringBuilder.append(cityId).append(separator)
    if (filters != null && filters.size() > 0) {
      filters.foreach { case (k, v) =>
        stringBuilder.append(k.trim).append(separator)
        stringBuilder.append(v.trim).append(separator)
      }
    }
    stringBuilder
  }

}
