package search.solr.client.log.impl

import java.util

import search.solr.client.log.SearchLog
import search.solr.client.storage.mongo.MongoStorage

/**
  * Created by soledede on 2016/2/24.
  */
class MongoSearchLogImpl extends SearchLog {
  override def write(keyWords: String, appKey: String, clientIp: String, userAgent: String, sourceType: String, cookies: String, userId: String, currentTime: java.util.Date): Unit = {
    val map = new util.HashMap[String, Object]()
    map.put("keyWords", keyWords)
    map.put("appKey", appKey)
    map.put("clientIp", clientIp)
    map.put("userAgent", userAgent)
    map.put("sourceType", sourceType)
    map.put("cookies", cookies)
    map.put("userId", userId)
    map.put("currentTime", currentTime)
    MongoStorage.saveMap("searchlogcollection", map)
  }

}

object MongoSearchLogImpl {
  var mongoSearchLogImpl: MongoSearchLogImpl = null

  def apply(): MongoSearchLogImpl = {
    if (mongoSearchLogImpl == null) mongoSearchLogImpl = new MongoSearchLogImpl()
    mongoSearchLogImpl
  }
}