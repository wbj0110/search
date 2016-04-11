package search.solr.client.listener

/**
 * Created by soledede on 2016/4/7.
 */
sealed trait TraceListenerEvent

case class AddIndex(content: String) extends TraceListenerEvent

case class DelLastIndex() extends TraceListenerEvent




