package search.solr.client.http

import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpEntityEnclosingRequestBase, HttpUriRequest, CloseableHttpResponse}
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HttpContext
import org.apache.http.concurrent.FutureCallback
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.client.utils.HttpClientUtils
import org.apache.http.client.config.RequestConfig
import org.apache.http.HttpHost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.HttpEntity
import org.apache.http.util.EntityUtils
import org.apache.http.HttpResponse
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper
import search.solr.client.entity.enumeration.HttpRequestMethodType
import search.solr.client.index.manager.IndexManagerRunner
import search.solr.client.util.Logging
import scala.collection.JavaConversions._

import scala.collection.mutable

class HttpClientUtil private extends Logging {

  // use Proxy
  def execute(request: HttpUriRequest, proxy: HttpHost, callback: (HttpContext, CloseableHttpResponse) => Unit): Unit = {
    val context: HttpClientContext = HttpClientContext.adapt(new BasicHttpContext);
    val reqCfg = RequestConfig.custom().setProxy(proxy).build();
    context.setRequestConfig(reqCfg)
    this.execute(request, context, callback)
  }

  def execute(request: HttpUriRequest, context: HttpContext, callback: (HttpContext, CloseableHttpResponse) => Unit): Unit = {
    var httpResp: CloseableHttpResponse = null
    try {
      if (context == null) {
        httpResp = HttpClientUtil.httpClient.execute(request)
      } else {
        httpResp = HttpClientUtil.httpClient.execute(request, context)
      }
      callback(context, httpResp)
    } catch {
      case t: Throwable => logError("Http Error", t)
    }
  }

  def execute(request: HttpUriRequest, callback: (HttpContext, CloseableHttpResponse) => Unit): Unit = {
    this.execute(request, null.asInstanceOf[HttpContext], callback)
  }

}

object HttpClientUtil {

  private val httpClient: CloseableHttpClient = HttpClients.createDefault();

  private val instance: HttpClientUtil = new HttpClientUtil

  def getInstance(): HttpClientUtil = {
    return instance
  }

  def closeHttpClient = HttpClientUtils.closeQuietly(httpClient);


  def requestHttp(url: String, requestType: HttpRequestMethodType.Type, paremeters: java.util.Map[String, Object], headers: java.util.Map[String, String], contexts: java.util.Map[String, String], callback: (HttpContext, HttpResponse) => Unit): Unit = {
    val request = HttpRequstUtil.createRequest(requestType, url)
    var isJson = false
    if (headers != null && !headers.isEmpty) {
      headers.foreach { header =>
        val key = header._1
        val value = header._2
        if (key.toLowerCase.trim.equalsIgnoreCase("content-type")) {
          if (value.toLowerCase.trim.equalsIgnoreCase("application/json")) isJson = true
        }
        request.addHeader(key, value)
      }
    }
    if (contexts != null && !contexts.isEmpty) {
      val context: HttpClientContext = HttpClientContext.adapt(new BasicHttpContext)
      contexts.foreach { attr =>
        context.setAttribute(attr._1, attr._2)
      }
    }

    if (paremeters != null && !paremeters.isEmpty) {
      val formparams: java.util.List[BasicNameValuePair] = new java.util.ArrayList[BasicNameValuePair]()
      paremeters.foreach { p =>
        formparams.add(new BasicNameValuePair(p._1, p._2.toString))
      }

      var entity: StringEntity = null
      if (isJson) {
        val mapper = new ObjectMapper()
        val jStirng = mapper.writeValueAsString(paremeters)
        println("string json:" + jStirng)
        entity = new StringEntity(jStirng, "utf-8");
      } else entity = new UrlEncodedFormEntity(formparams, "utf-8");

      request.asInstanceOf[HttpEntityEnclosingRequestBase].setEntity(entity)
    }
    HttpClientUtil.getInstance().execute(request, callback)
  }
}

object TestHttpClientUtil {

  def main(args: Array[String]): Unit = {
    //testHttp
    testHttpRecommend
  }

  def testHttpRecommend: Unit = {
    val url = "http://218.244.132.8:8088/recommend/sku"
    val parametersMap = new java.util.HashMap[String, java.lang.Object]()
    parametersMap.put("userId", "null")
    parametersMap.put("catagoryId", "null")
    parametersMap.put("brandId", "1421")
    parametersMap.put("number", Integer.valueOf(30))
    val headers = new mutable.HashMap[String, String]()
    headers("Content-Type") = "application/json"
    HttpClientUtil.requestHttp(url, HttpRequestMethodType.POST, parametersMap, headers,null, callback)
    def callback(context: HttpContext, httpResp: HttpResponse) = {
      try {
        println(Thread.currentThread().getName)
        println(httpResp)
        val sResponse = EntityUtils.toString(httpResp.getEntity)
        println(sResponse)
      } finally {
        HttpClientUtils.closeQuietly(httpResp)
      }
    }
  }

  def testHttp = {
    var start: Long = -1
    var end: Long = -1
    val request = HttpRequstUtil.createRequest(HttpRequestMethodType.GET, "http://121.40.241.26/recommend/0/5")
    def callback(context: HttpContext, httpResp: HttpResponse) = {
      try {
        println(Thread.currentThread().getName)
        println(httpResp)
        val sResponse = EntityUtils.toString(httpResp.getEntity)
        println()
        end = System.currentTimeMillis()
        printf("start %15d, end %15d, cost %15d \n", start, end, end - start)
        start = System.currentTimeMillis()
        val om = new ObjectMapper()
        val obj = om.readTree(sResponse)
        val rootJsonNode = obj.asInstanceOf[JsonNode]
        if (rootJsonNode.isArray) {
          val node = rootJsonNode.iterator()
          while (node.hasNext) {
            val obj = node.next()
            println(obj)
            val jnode = obj.getFields
            while (jnode.hasNext) {
              val it = jnode.next()
              val key = it.getKey
              val value = it.getValue
              println("key:" + key + "\n" + "value:" + value)
            }

          }

        }
        println(obj)
      } finally {
        HttpClientUtils.closeQuietly(httpResp)
      }
    }
    start = System.currentTimeMillis()

    //for(i <- 1 to 10){
    HttpClientUtil.getInstance().execute(request, callback)
    // }
    HttpClientUtil.closeHttpClient
  }

}