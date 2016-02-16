package search.solr.client.index.manager.impl

import java.io.PrintWriter
import java.util

import org.apache.http.HttpResponse
import org.apache.http.protocol.HttpContext
import org.apache.http.util.EntityUtils
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper
import search.solr.client.{SolrClientConf, SolrClient}
import search.solr.client.config.Configuration
import search.solr.client.entity.enumeration.HttpRequestMethodType
import search.solr.client.http.{HttpClientUtil, HttpRequstUtil}
import search.solr.client.index.manager.IndexManager
import search.solr.client.product.Producter
import search.solr.client.util.Logging
import scala.util.control.Breaks._

import scala.StringBuilder

/**
  * Created by soledede on 2016/2/15.
  */
class DefaultIndexManager private extends IndexManager with Logging with Configuration {
  val C_OR_UPDATE_XML = "_c_u.xml"
  val DELETE_XML = "_d.xml"

  var arrayObj: Array[String] = null

  if (multiValuedString != null && !multiValuedString.trim.equalsIgnoreCase("")) {
    arrayObj = multiValuedString.split(",")
  }

  val solrClient = SolrClient(new SolrClientConf())

  override def requestData(message: String): AnyRef = {
    var obj: AnyRef = null //return jsonNode
    if (message != null && !message.trim.equalsIgnoreCase("")) {
      logInfo(s"recieve$message")
      val msgArray = message.split(Producter.separator)
      if (msgArray.length <= 1) logError("input format not right,should '1423454543243-456  or 23545421334-34534534-5643'")
      else if (msgArray(0).trim.equals(Producter.DELETE)) {
        //command delete index
        logInfo(s"recieveDeleteMessage-$message")
        obj = msgArray
      } else {
        //add or update index
        if (msgArray.length == 2) {
          //have minimum update time
          val minUpdateTime = msgArray(0)
          val totalNum = msgArray(1)
          logInfo(s"recieveMessage-minUpdateTime:$minUpdateTime-totalNum:$totalNum")


          try {
            val request = HttpRequstUtil.createRequest(HttpRequestMethodType.GET, "http://121.40.241.26/recommend/0/5")
            HttpClientUtil.getInstance().execute(request, callback)
          } catch {
            case e: Exception =>
              logError("request faield!", e)
              logInfo(s"faieldMessage-minUpdateTime:$minUpdateTime-totalNum:$totalNum")
          }


        } else if (msgArray.length == 3) {
          //it's time quantum
          val startUpdateTime = msgArray(0)
          val endUpdataTime = msgArray(1)
          val totalNum = msgArray(2)
          logInfo(s"sendMessage-startTime:$startUpdateTime-endTime:$endUpdataTime-totalNum:$totalNum")
          try {
            val request = HttpRequstUtil.createRequest(HttpRequestMethodType.GET, "http://121.40.241.26/recommend/0/5")
            HttpClientUtil.getInstance().execute(request, callback)
          } catch {
            case e: Exception =>
              logError("request faield!", e)
              logInfo(s"faieldMessage-startTime:$startUpdateTime-endTime:$endUpdataTime-totalNum:$totalNum")
          }
        }
      }
    }
    //have get data
    def callback(context: HttpContext, httpResp: HttpResponse) = {
      val responseData = EntityUtils.toString(httpResp.getEntity)
      if (responseData != null && !responseData.equalsIgnoreCase("")) {
        val om = new ObjectMapper()
        obj = om.readTree(responseData)
      }
    }
    obj
  }


  override def geneXml(data: AnyRef): AnyRef = {
    var listMap: java.util.List[java.util.Map[java.lang.String, Object]] = new java.util.ArrayList[java.util.Map[java.lang.String, Object]]()
    var delList: java.util.List[java.lang.String] = null
    var writeToFileCnt = 0 //for loginfo
    var fileNamePreffix = C_OR_UPDATE_XML

    if (data != null) {
      val xml = new StringBuilder
      if (data.isInstanceOf[JsonNode]) {
        //generate add index xml
        val rootJsonNode = data.asInstanceOf[JsonNode]
        if (rootJsonNode.size() > 0) {
          writeToFileCnt = rootJsonNode.size()
          xml.append("<?xml version='1.0' encoding='UTF-8'?>")
          xml.append("\n")

          xml.append("<add>")
          xml.append("\n")

          if (rootJsonNode.isArray) {
            val arrayIt = rootJsonNode.iterator()
            while (arrayIt.hasNext) {
              val objNode = arrayIt.next()
              geneAddXml(objNode, xml, listMap)
            }
          }

          xml.append("</add>")


        }
      } else if (data.isInstanceOf[Array[String]]) {
        //generate delete index xml
        //delete xml
        fileNamePreffix = DELETE_XML
        val deleteArray = data.asInstanceOf[Array[String]]
        // deleteArray.
        if (deleteArray.length > 0) {
          writeToFileCnt = deleteArray.length -1
          xml.append("<?xml version='1.0' encoding='UTF-8'?>")
          xml.append("\n")
          xml.append("<delete>")
          xml.append("\n")
          delList = new java.util.ArrayList[java.lang.String]()
          for (i <- 1 to deleteArray.length - 1) {
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
        val fileName = System.currentTimeMillis() + fileNamePreffix
        val filePath = fileDir + fileName
        writeToDisk(xml.toString(), fileDir + fileName)
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

    val fields = obj.getFields
    while (fields.hasNext()) {
      val it = fields.next()
      val key = it.getKey
      val value = it.getValue
      if (key != null && value != null) {
        var isMultiValued = false
        if (arrayObj != null && arrayObj.length > 0) {
          breakable {
            for (i <- 0 to arrayObj.length - 1) {
              if (arrayObj(i).contains(key.trim)) {
                val multiValuedKeySeparator = arrayObj(i).split("=>")
                val vals = value.toString.split(multiValuedKeySeparator(1).trim) //multiValued Array
                vals.foreach(fieldAdd(xml, key, _))
                objMap.put(key, vals)
                isMultiValued = true
                break
              }
            }
          }
        }
        if (!isMultiValued) {
          //if not multivalued,need save singleValue to xml
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

  override def indexData(data: AnyRef): Boolean = {
    try {
      solrClient.addIndices(data, collection)
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
  override def delete(ids: java.util.ArrayList[java.lang.String]): Boolean = {
    solrClient.delete(ids, collection)
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
