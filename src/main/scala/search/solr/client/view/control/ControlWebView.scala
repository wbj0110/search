package search.solr.client.view.control

import javax.servlet.http.HttpServletRequest

import org.json4s.JValue
import search.solr.client.SolrClientConf
import search.solr.client.index.manager.impl.DefaultIndexManager
import search.solr.client.searchInterface.SearchInterface
import search.solr.client.util.{Util, Json4sHelp, Logging}
import search.solr.client.view.{PageUtil, WebView, WebViewPage}

import scala.collection.mutable
import scala.xml.Node
import search.solr.client.view.JettyUtil._


/**
  * Created by soledede on 2015/9/15.
  */
private[search] class ControlWebView(requestedPort: Int, conf: SolrClientConf) extends WebView(requestedPort, conf, name = "ControlWebView") with Logging {


  /** Initialize all components of the server. */
  override def initialize(): Unit = {
    attachHandler(createStaticHandler(WEB_STATIC_RESOURCE_DIR, "static"))
    val controlPage = new ControlWebPage()
    attachPage(controlPage)
  }

  initialize()
}

/*   <h1 style="color:red; text-align:center">welcome crawler!</h1>
         <svg width="100%" height="100%">
           <circle cx="500" cy="300" r="100" stroke="#ff0" stroke-width="5" fill="red"/>
           <polygon points="50 160 55 180 70 180 60 190 65 205 50 195 35 205 40 190 30 180 45 180"
                    stroke="green" fill="transparent" stroke-width="5"/>
           <text x="500" y="500" font-size="60" text-anchor="middle" fill="red">SVG</text>
           <svg width="5cm" height="4cm">
             <image xlink:href="http://s1.95171.cn/b/img/logo/95_logo_r.v731222600.png" x="0" y="0" height="100px" width="100px"/>
           </svg>
         </svg>*/

private[search] class ControlWebPage extends WebViewPage("solr") with PageUtil with Logging {


  override def render(request: HttpServletRequest): Seq[Node] = {

    val currentActiveCount = DefaultIndexManager.consumerManageThreadPool.getActiveCount

    val queryString = request.getQueryString

    try {
      if (queryString != null) {
        if (queryString.contains("switchCollection=")) {
          val isChoosenCollection = Util.regexExtract(queryString, "switchCollection=([a-zA-Z0-9]+)&?", 1)
          if (isChoosenCollection != null && !isChoosenCollection.toString.trim.equalsIgnoreCase(""))
            SearchInterface.switchCollection = isChoosenCollection.toString.toBoolean
        }
        if (SearchInterface.switchCollection) {
          if (queryString.contains("switchMg=")) {
            val mergeClouds = Util.regexExtract(queryString, "switchMg=([a-zA-Z0-9]+)&?", 1)
            if (mergeClouds != null && !mergeClouds.toString.trim.equalsIgnoreCase(""))
              SearchInterface.switchMg = mergeClouds.toString.trim
          }
          if (queryString.contains("switchSc=")) {
            val screenClouds = Util.regexExtract(queryString, "switchSc=([a-zA-Z0-9]+)&?", 1)
            if (screenClouds != null && !screenClouds.toString.trim.equalsIgnoreCase(""))
              SearchInterface.switchSc = screenClouds.toString.trim
          }
        }
      }
    } catch {
      case e: Exception => log.error("switch faield", e)
    }

    // println(isChoosenCollection)
    /*
           <img src="/image/log.png"/>{if currentActiveCount > 0) {
           <h4 style="color:red">Index is Running...</h4>
         }else{
           <h4 style="color:red">Index Finished</h4>
         }
         }*/
    //val allTaskNum = w.post(JobTaskAdded())
    val showPage = {
      <div>
        <img src="http://www.ehsy.com/images/logo.png"/>{if (currentActiveCount > 0) {
        <h4 style="color:red">Index is Running...</h4>

      } else {
        <h4 style="color:green">Index Finished</h4>
      }}{if (SearchInterface.switchMg != null && !"null".equalsIgnoreCase(SearchInterface.switchMg)) {
        <h4 style="color:yellow">current product collection:
          {SearchInterface.switchMg}
        </h4>
      }}<br/>{if (SearchInterface.switchSc != null && !"null".equalsIgnoreCase(SearchInterface.switchSc)) {
        <h4 style="color:yellow">current attribute collection:
          {SearchInterface.switchSc}
        </h4>
      }}

      </div>
    }
    assemblePage(showPage, "solr task trace")
  }

  override def renderJson(request: HttpServletRequest): JValue = Json4sHelp.writeTest

  def cTable(job: (String, mutable.HashMap[(String, String), Int])): Seq[Node] = {
    <table class="bordered">
      <thead>
        <tr>
          <th>JobId</th>
          <th>JobName</th>
          <th>Number</th>
        </tr>
      </thead>{job._2.map { s =>
      <tr>
        <td>
          {s._1._1}
        </td>
        <td>
          {s._1._2}
        </td>
        <td>
          {s._2}
        </td>
      </tr>
    }}
    </table>
  }
}

object ControlWebPage {
  val jobInfoCache: scala.collection.mutable.Map[String, mutable.HashMap[(String, String), Int]] = new scala.collection.mutable.HashMap[String, mutable.HashMap[(String, String), Int]]()
}