package search.solr.client.redis

import search.solr.client.config.Configuration
import search.solr.client.util.Logging

/**
  * Created by soledede on 2015/12/21.
  */
class JRedisImpl extends Redis with Logging {
  //general
/*  override def setValue[T: ClassTag](key: String, value: T, exSeconds: Long): Boolean = ???

  override def getValue[T: ClassTag](key: String): T = ???*/
}

object JRedisImpl extends Configuration {

}
