package search.solr.client.cache

import search.solr.client.cache.impl.RedisCache

import scala.reflect.ClassTag


/**
  * Created by soledede on 2015/12/18.
  */
trait KVCache {

  //save one week
  def incrby(key: String, step: Int = 1, expiredTime: Long = 60 * 60 * 24*30): Int

  def get(key: String):Int

  def keys(preffixKey: String): Seq[String]


}

object KVCache { 
  def apply(kvType: String = "redis"): KVCache = {
    kvType match {
      case "redis" => RedisCache()
      case _ => null
    }

  }
}
