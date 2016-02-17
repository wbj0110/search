package search.solr.client.index.manager

import search.solr.client.index.manager.impl.DefaultIndexManager
import search.solr.client.util.Logging

/**
  * Created by soledede on 2016/2/15.
  */
trait IndexManager extends Logging {

  def requestData(message: String): AnyRef

  def geneXml(data: AnyRef): AnyRef

  def indexData(data: AnyRef,collection: String): Boolean

  def delete(ids: java.util.ArrayList[java.lang.String],collection: String): Boolean

}

object IndexManager {
  def apply(indexer: String = "default"): IndexManager = {
    indexer match {
      case "default" => DefaultIndexManager()
      case _ => null
    }
  }
}
