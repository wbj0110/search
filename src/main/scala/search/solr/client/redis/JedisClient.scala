package search.solr.client.redis

import org.apache.commons.lang3.StringUtils
import redis.clients.jedis.{JedisPool, JedisPoolConfig, Jedis}
import search.solr.client.config.Configuration

/**
  * Created by soledede on 2016/4/16.
  */
private[search] object JedisClient extends Configuration {

  val lockPool = new Object()

  var pool: JedisPool = null

  def createJredis(): Jedis = {
    val jedis = new Jedis(redisHost, redisPort)
    jedis
  }

  def createJredis(host: String, port: Int): Jedis = {
    val jedis = new Jedis(host, port)
    jedis
  }

  def createJredis(host: String, port: Int, password: String): Jedis = {
    val jedis = new Jedis(host, port)
    if (!StringUtils.isNotBlank(password))
      jedis.auth(password)
    jedis
  }


  def createJedisPool(): JedisPool = {
    //collection pool config
    val config = new JedisPoolConfig()
    //setup maxmum collection number
    config.setMaxTotal(1000)
    //setup maxmum block
    config.setMaxWaitMillis(1000 * 2)
    config.setMaxIdle(10)
    val jedis = new JedisPool(config, redisHost, redisPort)
    jedis
  }


  def poolInit() = {
    if (pool == null) {
      lockPool.synchronized {
        pool = createJedisPool()
      }
    }
  }

  def getRedisFromPool():Jedis = {
    if (pool == null)
      poolInit()
    pool.getResource
  }

  def returnRedis(redis: Jedis) = {
    pool.returnResource(redis)
  }
}
