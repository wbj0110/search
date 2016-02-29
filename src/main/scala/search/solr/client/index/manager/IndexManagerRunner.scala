package search.solr.client.index.manager

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.protocol.HttpContext
import search.solr.client.entity.enumeration.HttpRequestMethodType
import search.solr.client.http.HttpClientUtil

import scala.collection.mutable

/**
  * Created by soledede on 2016/2/29.
  */
class IndexManagerRunner(inexer: IndexManager, request: HttpRequestBase, context: HttpClientContext, callback: (HttpContext, HttpResponse) => Unit) extends Runnable {

  override def run(): Unit = {
    inexer.execute(request, context, callback)
  }
}
