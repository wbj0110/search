package search.solr.client.config

import com.typesafe.config.ConfigFactory
import search.solr.client.util.Util

import scala.util.Try

/**
  * Created by soledede on 2016/2/14.
  */
trait Configuration {
  /**
    * Application config object.
    */
  val config = ConfigFactory.load()

  /**
    * kafka
    */
  lazy val brokers = Try(config.getString("kafka.brokers")).getOrElse("localhost:9092")
  lazy val productTopic = Try(config.getString("kafka.producter.topic")).getOrElse("test")
  lazy val productType = Try(config.getString("kafka.producter.type")).getOrElse("async")
  lazy val serializerClass = Try(config.getString("kafka.serializer.class")).getOrElse("kafka.serializer.StringEncoder")
  lazy val consumerTopic = Try(config.getString("kafka.consumer.topic")).getOrElse("test")
  lazy val zk = Try(config.getString("zk")).getOrElse("localhost:2181")
  lazy val groupId = Try(config.getString("kafka.groupid")).getOrElse("group1")

  lazy val solrBaseUrls = Try(config.getString("solr.baseUrls")).getOrElse("localhost:9092")

  /**
    * zk backup for search balance
    */
  lazy val zkBackUp = Try(config.getString("zkBackUp")).getOrElse("localhost:2181")

  /**
    * generate xml
    */
  lazy val multiValuedString = Try(config.getString("field.multivalued")).getOrElse("")
  lazy val filedirMergeCloud = Try(config.getString("filedir.mergeclouds")).getOrElse("")
  lazy val filedirScreenCloud = Try(config.getString("filedir.screenclouds")).getOrElse("")

  /**
    * filter
    *
    */

  lazy val filterChanges = Try(config.getString("filter.change")).getOrElse("")

  /**
    * solr
    */

  lazy val collection = Try(config.getString("collection")).getOrElse("")

  lazy val defaultCollection = Try(config.getString("defaultcollection")).getOrElse("")

  lazy val defaultAttrCollection = Try(config.getString("defaultattrcollection")).getOrElse("")


  lazy val defaultSuggestCollection = Try(config.getString("defaultsuggestcollection")).getOrElse("")


  /**
    * remote http url
    */
  lazy val mergesCloudsUrl = Try(config.getString("url.mergeclouds")).getOrElse("http://localhost:8088/mergeclouds")
  lazy val screenCloudsUrl = Try(config.getString("url.screenclouds")).getOrElse("http://localhost:8088/screenclouds")

  //page
  lazy val pageSize = Try(config.getInt("pageSize")).getOrElse(10)


  //cache time  second

  lazy val cacheTime = Try(config.getInt("cache.time")).getOrElse(5)
  /**
    * threads pool
    */

  lazy val consumerThreadsNum = Try(config.getInt("consumer.threads.number")).getOrElse(0)

  lazy val consumerCoreThreadsNum = Try(config.getInt("consumer.core.threads.number")).getOrElse(2)

  lazy val threadsWaitNum = Try(config.getInt("threads.wait.number")).getOrElse(50000)

  lazy val threadsSleepTime = Try(config.getInt("threads.sleep")).getOrElse(1000)


  //redis
  lazy val redisHost = Try(config.getString("redis.host")).getOrElse("localhost")
  lazy val redisPort = Try(config.getInt("redis.port")).getOrElse(6379)

  //monitoru host
  lazy val monitorConfigHost = Try(config.getString("monitor.host")).getOrElse(Util.localHostNameForURI())
  var monitorHost = "127.0.0.1"
  if (monitorConfigHost != null) monitorHost = monitorConfigHost
  lazy val monitorPort = Try(config.getInt("monitor.port")).getOrElse(9999)

  //web
  val WEB_STATIC_RESOURCE_DIR = "static"

  /**
    * log4j
    */

  lazy val logShow = Try(config.getBoolean("log.show")).getOrElse(true)

}
