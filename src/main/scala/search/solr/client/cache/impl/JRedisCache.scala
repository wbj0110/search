package search.solr.client.cache.impl

import search.solr.client.cache.KVCache
import search.solr.client.redis.Redis
import search.solr.client.util.Logging

import scala.reflect.ClassTag

/**
  * Created by soledede on 2016/4/16.
  */
class JRedisCache private extends KVCache with Logging {
  val jRedis = Redis("jredis")

  //save one week
  override def incrby(key: String, step: Int, expiredTime: Long): Int = -1

  override def get(key: String): Int = -1


  override def getObj[T: ClassTag](key: String): T = {
    jRedis.get[T](key)
  }

  override def put[T: ClassTag](key: String, value: T,seconds:Int): Unit = {
    jRedis.put(key, value,seconds)
  }

  override def keys(preffixKey: String): Seq[String] = null
}

object JRedisCache {
  var redisCache: JRedisCache = null

  def apply(): JRedisCache = {
    if (redisCache == null) redisCache = new JRedisCache
    redisCache
  }
}
