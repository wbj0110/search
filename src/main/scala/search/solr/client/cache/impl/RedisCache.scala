package search.solr.client.cache.impl

import search.solr.client.cache.KVCache
import search.solr.client.redis.Redis
import search.solr.client.util.Logging

import scala.reflect.ClassTag

/**
  * Created by soledede on 2015/12/18.
  */
class RedisCache private extends KVCache with Logging {
  val redis = Redis()

  //save one week
  override def incrby(key: String, step: Int, expiredTime: Long): Int = {
    redis.incrBy(key, step)
  }

  override def get(key: String): Int = {
    val v = redis.getValue[String](key)
    if (v == null) 0
    else {
      val value = v.getOrElse("0")
      if (value == null || value.equalsIgnoreCase("") || value.equalsIgnoreCase("0")) 0
      else Integer.valueOf(value)
    }
  }

  override def keys(preffixKey: String): Seq[String] = {
    redis.keys(preffixKey).getOrElse(null)
  }
}

object RedisCache {
  var redisCache: RedisCache = null

  def apply(): RedisCache = {
    if (redisCache == null) redisCache = new RedisCache
    redisCache
  }

}

