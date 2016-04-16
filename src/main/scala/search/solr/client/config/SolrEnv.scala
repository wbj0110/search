package search.solr.client.config

import search.solr.client.SolrClientConf
import search.solr.client.serializer.Serializer
import search.solr.client.util.Logging

/**
 * @author soledede
 */
private[search]
class SolrEnv (val conf: SolrClientConf, val serializer: Serializer) extends Logging {
}


object SolrEnv extends Logging {
  @volatile private var env: SolrEnv = _


  def set(e: SolrEnv) {
    env = e
  }

  def get: SolrEnv = {
    env
  }

  def init(conf: SolrClientConf,ser: Serializer ){
    new SolrEnv(conf,ser)
  }
  }


