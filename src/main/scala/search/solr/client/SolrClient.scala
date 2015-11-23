package search.solr.client

import search.solr.client.impl.SolJSolrCloudClient

import scala.reflect.ClassTag

/**
  * Created by soledede on 2015/11/16.
  */
private[search] trait SolrClient {

  def searchByQuery[T: ClassTag](query: T,collection:String = "searchcloud"): AnyRef = null

  def updateIndices[D: ClassTag](doc: D,collection:String = "searchcloud"): Unit = {}

  def addIndices[D: ClassTag](doc: D,collection:String = "searchcloud"): Unit = {}

  def close(): Unit = {}

  def closeKw(): Unit = {}
}

private[search] object SolrClient {

  def apply(conf: SolrClientConf, cType: String = "solrJSolrCloud"):SolrClient = {
    cType match {
      case "solrJSolrCloud" => SolJSolrCloudClient(conf)
      case _ => null
    }
  }
}
