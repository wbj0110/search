package search.solr.client.redis

import search.solr.client.config.Configuration

/**
  * Created by soledede on 2015/12/18.
  */
object RedisClient extends Configuration {

  implicit val akkaSystem = akka.actor.ActorSystem()
  val redisClient = redis.RedisClient(port = redisPort, host = redisHost)

  def apply() = {
    redisClient
  }

  def close() = {
    redisClient.shutdown()
    akkaSystem.shutdown()
  }

}
