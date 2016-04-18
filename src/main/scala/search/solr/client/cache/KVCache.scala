package search.solr.client.cache

import search.solr.client.cache.impl.{JRedisCache, RedisCache}
import search.solr.client.entity.searchinterface.FilterAttributeSearchResult

import scala.reflect.ClassTag


/**
  * Created by soledede on 2015/12/18.
  */
trait KVCache {

  //save one week
  def incrby(key: String, step: Int = 1, expiredTime: Long = 60 * 60 * 24 *5): Int

  def get(key: String): Int = -1

  def getObj[T: ClassTag](key: String): T = null.asInstanceOf[T]

  def put[T: ClassTag](key: String, value: T,seconds:Int): Unit = {}

  def keys(preffixKey: String): Seq[String] = null.asInstanceOf[Seq[String]]


}

object KVCache {
  def apply(kvType: String = "redis"): KVCache = {
    kvType match {
      case "redis" => RedisCache()
      case "jRedis" => JRedisCache()
      case _ => null
    }
  }

    def main(args: Array[String]) {
      testKV
    }

    def testKV() = {
      val kvCache = KVCache("jRedis")
      kvCache.put("t1", new FilterAttributeSearchResult(),5)
      val ko1 = kvCache.get("t1")
      println(ko1)
    }
}
