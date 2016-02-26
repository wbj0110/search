package search.solr.client.config

import com.typesafe.config.ConfigFactory

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

  /**
    * generate xml
    */
  lazy val multiValuedString = Try(config.getString("field.multivalued")).getOrElse("")
  lazy val filedirMergeCloud = Try(config.getString("filedir.mergeclouds")).getOrElse("")
  lazy val filedirScreenCloud = Try(config.getString("filedir.screenclouds")).getOrElse("")

  /**
    * solr
    */

  lazy val collection = Try(config.getString("collection")).getOrElse("")

  /**
    * remote http url
    */
  lazy val mergesCloudsUrl = Try(config.getString("url.mergeclouds")).getOrElse("http://localhost:8088/mergeclouds")
  lazy val screenCloudsUrl = Try(config.getString("url.screenclouds")).getOrElse("http://localhost:8088/screenclouds")

  //page
  lazy val pageSize = Try(config.getInt("pageSize")).getOrElse(10)

}
