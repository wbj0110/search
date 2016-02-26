package search.solr.client.index.manager.impl

import java.io.PrintWriter
import java.util


import org.apache.http.HttpResponse
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.{BasicHttpContext, HttpContext}
import org.apache.http.util.EntityUtils
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper
import search.solr.client.consume.Consumer._
import search.solr.client.{SolrClientConf, SolrClient}
import search.solr.client.config.Configuration
import search.solr.client.entity.enumeration.HttpRequestMethodType
import search.solr.client.http.{HttpClientUtil, HttpRequstUtil}
import search.solr.client.index.manager.IndexManager
import search.solr.client.product.Producter
import search.solr.client.util.Logging
import scala.collection.mutable
import scala.util.control.Breaks._
import scala.collection.JavaConversions._

import scala.StringBuilder

/**
  * Created by soledede on 2016/2/15.
  */
class DefaultIndexManager private extends IndexManager with Logging with Configuration {
  val C_OR_UPDATE_XML = "_c_u.xml"
  val DELETE_XML = "_d.xml"


  // "http://192.168.51.54:8088/mergecloud"
  val MERGECLOUD_URL: String = mergesCloudsUrl
  val SCREEN_URL: String = screenCloudsUrl

  val needSenseCharcter = Array("|") //if you need use new sense charcter in application.conf ,you need add it to array

  var arrayObj: Array[String] = null

  if (multiValuedString != null && !multiValuedString.trim.equalsIgnoreCase("")) {
    arrayObj = multiValuedString.split("&")
  }

  val solrClient = SolrClient(new SolrClientConf())

  override def requestData(message: String): AnyRef = {
    var obj: AnyRef = null //return jsonNode
    if (message != null && !message.trim.equalsIgnoreCase("")) {
      logInfo(s"recieve$message")
      val msgArray = message.split(Producter.separator)
      if (msgArray.length <= 2) logError("input format not right,should 'mergescloud-1423454543243-456  or mergescloud-23545421334-34534534-5643 or mergescloud-delete-324234-4343423'")
      else if (msgArray(1).trim.equals(Producter.DELETE)) {
        //command delete index
        logInfo(s"recieveDeleteMessage-$message")
        obj = msgArray
      } else {
        //add or update index
        if (msgArray.length == 3) {
          //first represent collection ,mergescloud-234343211-34
          //have minimum update time
          val collection = msgArray(0)
          val minUpdateTime = msgArray(1)
          val totalNum = msgArray(2)
          logInfo(s"recieveMessage-minUpdateTime:$minUpdateTime-totalNum:$totalNum")

          try {
            requestUrl(collection, totalNum, minUpdateTime, null)
          } catch {
            case e: Exception =>
              logError("request faield!", e)
              logInfo(s"faieldMessage-minUpdateTime:$minUpdateTime-totalNum:$totalNum")
          }
        } else if (msgArray.length == 4) {
          //first represent collection ,mergescloud-2343433212-234343211-34
          //it's time quantum
          val collection = msgArray(0).trim
          val startUpdateTime = msgArray(1).trim
          val endUpdataTime = msgArray(2).trim
          val totalNum = msgArray(3).trim
          logInfo(s"sendMessage-startTime:$startUpdateTime-endTime:$endUpdataTime-totalNum:$totalNum")
          try {
            requestUrl(collection, totalNum, startUpdateTime, endUpdataTime)
          } catch {
            case e: Exception =>
              logError("request faield!", e)
              logInfo(s"faieldMessage-startTime:$startUpdateTime-endTime:$endUpdataTime-totalNum:$totalNum")
          }
        }
      }
    }

    /**
      *
      * @param totalNum
      * @param startUpdateTime
      * @param endUpdataTime
      */
    def requestUrl(collection: String, totalNum: String, startUpdateTime: String, endUpdataTime: String): Unit = {
      var url = MERGECLOUD_URL
      collection match {
        case "screencloud" => url = SCREEN_URL
        case "mergescloud" => url = MERGECLOUD_URL
        case _ =>
      }


      var more = 0
      if (totalNum.toInt % pageSize != 0) more = totalNum.toInt % pageSize
      var onePage = 0
      if (more > 0) onePage = 1
      val requestCounts = (totalNum.toInt / pageSize) + onePage

      for (i <- 0 to requestCounts - 1) {
        val paremeters = new mutable.HashMap[String, String]()
        if (startUpdateTime != null && !startUpdateTime.trim.equalsIgnoreCase("null") && !startUpdateTime.trim.equalsIgnoreCase("") && startUpdateTime.trim.equalsIgnoreCase("0"))
          paremeters("startUpdateTime") = startUpdateTime
        if (endUpdataTime != null && !startUpdateTime.trim.equalsIgnoreCase("null") && !endUpdataTime.trim.equalsIgnoreCase("") && endUpdataTime.trim.equalsIgnoreCase("0"))
          paremeters("endUpdateTime") = endUpdataTime
        paremeters("start") = (i * pageSize).toString
        paremeters("rows") = pageSize.toString
        requestHttp(collection, url, HttpRequestMethodType.POST, paremeters, callback)
      }

    }

    //have get data
    def callback(context: HttpContext, httpResp: HttpResponse) = {
      val collection = context.getAttribute("collection").toString
      val responseData = EntityUtils.toString(httpResp.getEntity)

      if (responseData != null && !responseData.equalsIgnoreCase("")) {
        val om = new ObjectMapper()
        obj = om.readTree(responseData)

        indexOrDelteData

      }


      def indexOrDelteData: Unit = {
        val xmlBool = geneXml(obj, collection)
        if (xmlBool != null) {
          indexData(collection, xmlBool)
          /*if (xmlBool.isInstanceOf[util.ArrayList[String]]) {
            deleteIndexData(collection, xmlBool)
          }
          else {
            indexData(collection, xmlBool)
          }*/
        }
      }
      /**
        * index data
        * @param collection
        * @param xmlBool
        */
      def indexData(collection: java.lang.String, xmlBool: AnyRef): Unit = {
        try {
          val indexData = xmlBool.asInstanceOf[java.util.ArrayList[java.util.Map[java.lang.String, Object]]]
          indexData(0).asInstanceOf[java.util.Map[java.lang.String, Object]]
          if (indexer.indexData(indexData, collection)) logInfo(" index success!")
          else {
            logError("index faield!Ids:")
            indexData.foreach { doc =>
              logInfo(s"index faield id:\t${doc.get("id")}")
            }
          }
        } catch {
          case castEx: java.lang.ClassCastException =>
            deleteIndexData(collection, xmlBool)
          // case e: Exception => logError("index faield", e)
        }

      }
    }

    //  obj
    null
  }


  def requestHttp(collection: String, url: String, requestType: HttpRequestMethodType.Type, paremeters: mutable.Map[String, String], callback: (HttpContext, HttpResponse) => Unit): Unit = {
    val request = HttpRequstUtil.createRequest(requestType, url)
    val context: HttpClientContext = HttpClientContext.adapt(new BasicHttpContext)
    context.setAttribute("collection", collection)
    if (paremeters != null && !paremeters.isEmpty) {
      val formparams: java.util.List[BasicNameValuePair] = new java.util.ArrayList[BasicNameValuePair]()
      paremeters.foreach { p =>
        formparams.add(new BasicNameValuePair(p._1, p._2))
      }
      val entity: UrlEncodedFormEntity = new UrlEncodedFormEntity(formparams, "utf-8");
      request.asInstanceOf[HttpEntityEnclosingRequestBase].setEntity(entity)
    }
    HttpClientUtil.getInstance().execute(request, context, callback)
  }


  override def geneXml(data: AnyRef, collection: String): AnyRef = {
    var listMap: java.util.List[java.util.Map[java.lang.String, Object]] = new java.util.ArrayList[java.util.Map[java.lang.String, Object]]()
    var delList: java.util.List[java.lang.String] = null
    var writeToFileCnt = 0 //for loginfo
    var fileNamePreffix = C_OR_UPDATE_XML

    if (data != null) {
      val xml = new StringBuilder
      if (data.isInstanceOf[JsonNode]) {
        //generate add index xml
        val dataJsonNode = data.asInstanceOf[JsonNode]

        if (!dataJsonNode.isNull && dataJsonNode.size() > 0) {

          val rootJsonNode = dataJsonNode.get("data")
          if (!rootJsonNode.isNull && rootJsonNode.size() > 0) {

            xml.append("<?xml version='1.0' encoding='UTF-8'?>")
            xml.append("\n")

            xml.append("<add>")
            xml.append("\n")

            if (rootJsonNode.isArray) {
              writeToFileCnt = rootJsonNode.size()
              val arrayIt = rootJsonNode.iterator()
              while (arrayIt.hasNext) {
                val objNode = arrayIt.next()
                geneAddXml(objNode, xml, listMap)
              }
            } else {
              writeToFileCnt = 1
              geneAddXml(rootJsonNode, xml, listMap) //single document
            }


            xml.append("</add>")
          }
        }
      } else if (data.isInstanceOf[Array[String]]) {
        //generate delete index xml
        //delete xml
        fileNamePreffix = DELETE_XML
        val deleteArray = data.asInstanceOf[Array[String]]
        // deleteArray.
        if (deleteArray.length > 2) {
          writeToFileCnt = deleteArray.length - 2
          xml.append("<?xml version='1.0' encoding='UTF-8'?>")
          xml.append("\n")
          xml.append("<delete>")
          xml.append("\n")
          delList = new java.util.ArrayList[java.lang.String]()
          for (i <- 2 to deleteArray.length - 1) {
            //index 0 reoresent command DELETE
            xml.append("<id>")
            xml.append(deleteArray(i).trim)
            xml.append("</id>")
            xml.append("\n")
            delList.add(deleteArray(i).trim)
          }
          xml.append("</delete>")
          // delArray = deleteArray.drop(1)
        }
      }
      if (!xml.isEmpty) {
        val fileName = collection + "_" + System.currentTimeMillis() + fileNamePreffix
        var filePath = filedirMergeCloud + fileName
        if (collection.equalsIgnoreCase("screencloud")) filePath = filedirScreenCloud + fileName
        writeToDisk(xml.toString(), filePath)
        logInfo(s"write file $filePath success,Total ${writeToFileCnt} documents！")
      }

    }
    if (listMap.size() > 0) listMap
    else if (delList != null && delList.size() > 0) delList
    else null
  }

  def geneAddXml(obj: JsonNode, xml: StringBuilder, listMap: java.util.List[java.util.Map[java.lang.String, Object]]) = {
    xml.append("<doc>")
    xml.append("\n")

    val objMap: java.util.Map[java.lang.String, AnyRef] = new java.util.HashMap[java.lang.String, AnyRef]() //for add index

    val fields = obj.getFields //get all fields
    while (fields.hasNext()) {
      val it = fields.next()
      val key = it.getKey
      var value = it.getValue.toString

      if (key != null && value != null) {
        var isMultiValued = false
        if (arrayObj != null && arrayObj.length > 0) {
          //arrayObj represent have mutivalued field config
          breakable {
            for (i <- 0 to arrayObj.length - 1) {
              if (arrayObj(i).contains(key.trim)) {
                val multiValuedKeySeparator = arrayObj(i).split("=>")
                val separator = multiValuedKeySeparator(1).trim
                var needSense = false
                breakable {
                  //judge whether need sense
                  for (i <- 0 to needSenseCharcter.length - 1) {
                    if (needSenseCharcter(i).trim.equalsIgnoreCase(separator)) {
                      needSense = true
                      break
                    }
                  }
                }
                var vals: Array[String] = null
                if (needSense)
                  vals = value.split(s"\\$separator") //multiValued Array
                else vals = value.split(s"$separator") //multiValued Array
                val listMutivalued = new util.ArrayList[java.lang.String]()
                vals.foreach { f =>
                  var fV: String = f.replaceAll("\"(\\S+)\"", "$1")
                  fV = fV.replaceAll("\"(\\S+)", "$1")
                  fV = fV.replaceAll("(\\S+)\"", "$1")
                  listMutivalued.add(fV)
                  fieldAdd(xml, key, fV)
                }
                objMap.put(key, listMutivalued)
                isMultiValued = true
                break
              }
            }
          }
        }
        if (!isMultiValued) {
          //if not multivalued,need save singleValue to xml
          value = value.replaceAll("\"(\\S+)\"", "$1")
          fieldAdd(xml, key, value)
          objMap.put(key, value)
        }

      }
    }
    if (!objMap.isEmpty) listMap.add(objMap)
    xml.append("</doc>")
    xml.append("\n")
  }

  def fieldAdd(xml: StringBuilder, key: String, value: Object) = {
    xml.append("<field name=\"")
    xml.append(key)
    xml.append("\">")
    xml.append("<![CDATA[")
    xml.append(value)
    xml.append("]]>")
    xml.append("</field>")
    xml.append("\n")
  }

  override def indexData(data: AnyRef, collection: String): Boolean = {
    try {
      val r = solrClient.addIndices(data, collection)
      //solrClient.close()
      r
      true
    } catch {
      case e: Exception => false
    }
  }

  /**
    * delete index
    * eg: delete-234523-34534
    * @param ids
    * @return
    */
  override def delete(ids: java.util.ArrayList[java.lang.String], collection: String): Boolean = {
    val r = solrClient.delete(ids, collection)
    //solrClient.close()
    r
  }

  private def writeToDisk(xml: String, filePath: String) = {
    val w = new PrintWriter(filePath)
    w.println(xml)
    w.close()
  }
}

object DefaultIndexManager {
  var indexManager: DefaultIndexManager = null

  def apply(): DefaultIndexManager = {
    if (indexManager == null) indexManager = new DefaultIndexManager()
    indexManager
  }
}
