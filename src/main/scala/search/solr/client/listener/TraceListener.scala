package search.solr.client.listener

import search.solr.client.util.Logging

/**
 * Created by soledede on 2016/4/8.
 */
trait TraceListener extends Logging{

  def onAddIndex(content: AddIndex)

  def onDelLastIndex()

  def onSolrCollectionTimeout()

  def onNodeNoHealth()

  def onSwitchSolrServer(server: String)
}
