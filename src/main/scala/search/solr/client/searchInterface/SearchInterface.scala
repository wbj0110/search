package search.solr.client.searchInterface

import java.util

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrQuery.ORDER
import org.apache.solr.client.solrj.response.QueryResponse
import search.solr.client.entity.searchinterface._
import search.solr.client.util.Logging
import search.solr.client.{SolrClientConf, SolrClient}
import scala.StringBuilder
import scala.collection.JavaConversions._


/**
  * Created by soledede on 2016/2/20.
  */
object SearchInterface extends Logging {

  val spellcheckSeparator = "$__$"


  val solrClient = SolrClient(new SolrClientConf())


  /**
    *
    * search by keywords,must record searchlog for log analysis
    * @param keyWords eg:螺丝钉
    * @param cityId eg:111
    * @param sorts  eg:Map(price->desc,sales->desc,score->desc)
    * @param start eg:0
    * @param rows eg:10
    * @return SearchResult
    */
  def searchByKeywords(keyWords: java.lang.String, cityId: java.lang.Integer, sorts: java.util.Map[java.lang.String, java.lang.String], start: java.lang.Integer, rows: java.lang.Integer): SearchResult = {
    val msg = new Msg()
    val searchResult = new SearchResult()
    if (keyWords == null && cityId == null) {
      msg.setMsg("keyWords and cityId not null")
      searchResult.setMsg(msg)
      return searchResult
    }
    else if (keyWords == null) {
      msg.setMsg("keyWords not null")
      searchResult.setMsg(msg)
      return searchResult
    }
    else if (cityId == null) {
      msg.setMsg("cityId not null")
      searchResult.setMsg(msg)
      return searchResult
    }

    //page
    var sStart: Int = 0
    var sRows: Int = 10

    if (start != null && start > 0) sStart = start
    if (rows != null && rows > 0) sRows = rows


    val keyWord = keyWords.trim
    val keyWordsModel = s"(original:$keyWord^50) OR (sku:$keyWord^50) OR (brandZh_ps$keyWord^30) OR (brandEn_ps:$keyWord^30) OR (sku:*$keyWord*^11) OR (original:*$keyWord*^10) OR (text:$keyWord^2) OR (pinyin:$keyWord^0.002)"

    val fq = s"deliveryTime:0 OR cityId:$cityId"


    val query: SolrQuery = new SolrQuery
    query.set("qt", "/select")
    query.setQuery(keyWordsModel)
    query.setFilterQueries(fq)

    //sort
    val sortsString = new StringBuilder
    if (sorts != null && sorts.size() > 0) {
      // eg:  query.addSort("price", SolrQuery.ORDER.desc)
      sorts.foreach { sortOrder =>
        val field = sortOrder._1
        val orderString = sortOrder._2.trim
        var order: ORDER = null
        orderString match {
          case "desc" => order = SolrQuery.ORDER.desc
          case "asc" => order = SolrQuery.ORDER.asc
          case _ => SolrQuery.ORDER.desc
        }
        query.addSort(field, order)
      }
    }

    //page
    query.setStart(sStart)
    query.setRows(sRows)

    val r = solrClient.searchByQuery(query, "mergescloud")
    var result: QueryResponse = null
    if (r != null) result = r.asInstanceOf[QueryResponse]

    val resultSearch = getSearchResult(result) //get response result
    if (resultSearch != null && resultSearch.size > 0) searchResult.setResult(resultSearch) //set response resut

    //  highlighting
    val highlighting = getHighlightingList(result)
    if (highlighting != null && highlighting.size() > 0) searchResult.setHighlighting(highlighting)

    //spellcheck
    val spellchecks = getSpellCheckList(result)

    if (spellchecks != null && spellchecks.size() > 0) searchResult.setSpellChecks(spellchecks)

    msg.setMsg("success!")
    msg.setCode(0)
    searchResult.setMsg(msg)
    searchResult
  }

  /**
    *
    * get spellcheck list
    * @param result
    * @return
    */
  private def getSpellCheckList(result: QueryResponse): java.util.HashMap[java.lang.String, java.util.List[java.lang.String]] = {
    if (result != null) {
      val spellChecks = new java.util.HashMap[java.lang.String, java.util.List[java.lang.String]]()
      val spellcheckResponse = result.getSpellCheckResponse
      if (spellcheckResponse != null) {
        val collateResults = spellcheckResponse.getCollatedResults
        val spellcheckCorrectionsSet = new util.HashSet[java.lang.String]()
        if (collateResults != null && collateResults.size() > 0) {
          collateResults.foreach { collation =>
            val missspellingCorrection = collation.getMisspellingsAndCorrections
            if (missspellingCorrection != null && missspellingCorrection.size() > 0) {
              missspellingCorrection.foreach { correction =>
                val originalWord = correction.getOriginal
                val correctionWord = correction.getCorrection
                spellcheckCorrectionsSet.add(originalWord + spellcheckSeparator + correctionWord)
              }
            }
          }
        }
        if (spellcheckCorrectionsSet.size() > 0) {
          spellcheckCorrectionsSet.foreach { spellcheck =>
            val spellcheckArray = spellcheck.split(spellcheckSeparator)
            val original = spellcheckArray(0)
            val correct = spellcheckArray(1)
            if (spellChecks.contains(original)) {
              val correctList = spellChecks.get(original)
              correctList.add(correct)
              spellChecks.put(original, correctList)
            } else {
              val initialCorrectList = new java.util.ArrayList[java.lang.String]()
              initialCorrectList.add(correct)
              spellChecks.put(original, initialCorrectList)
            }
          }
        }

      }
      spellChecks
    } else null
  }

  /**
    *
    * get highlighting list
    * @param result
    * @return
    */
  private def getHighlightingList(result: QueryResponse): java.util.Map[java.lang.String, java.util.Map[java.lang.String, java.util.List[java.lang.String]]] = {
    if (result != null) {
      return result.getHighlighting
    } else null
  }

  /**
    *
    * get response Result
    * @param result
    * @return
    */
  private def getSearchResult(result: QueryResponse): java.util.List[util.Map[java.lang.String, Object]] = {
    val resultList: java.util.List[util.Map[java.lang.String, Object]] = new java.util.ArrayList[util.Map[java.lang.String, Object]]() //search result
    //get Result
    if (result != null) {
      val response = result.getResults
      response.foreach { doc =>
        val resultMap: util.Map[String, Object] = new util.HashMap[String, Object]()
        val fields = doc.getFieldNames
        fields.foreach { fieldName =>
          resultMap.put(fieldName, doc.getFieldValue(fieldName))
        }
        if (!resultMap.isEmpty)
          resultList.add(resultMap)
      }
    }
    resultList
  }

  /**
    *
    * who where when what
    * @param keyWords
    * @param clientIp
    * @param userAgent
    * @param sourceType
    * @param userId
    */
  def recordSearchLog(keyWords: java.lang.String, clientIp: java.lang.String, userAgent: java.lang.String, sourceType: java.lang.String, userId: java.lang.String): Unit = {
    val currentTime = null
  }

  /**
    *
    * get filter atrtributes by catagoryid
    * @param catagoryId
    * @return FilterAttribute
    */
  def searchFilterAttributeByCatagoryId(catagoryId: java.lang.String): java.util.List[FilterAttribute] = {
    null
  }


  /**
    *
    * Tips: front should keep the attributeName cache by searchFilterAttributeByCatagoryId
    * @param keyWords
    * @param catagoryId
    * @param cityId
    * @param sorts   eg:Map(price->desc,sales->desc,score->desc)
    * @param filters eg:Map("t89_s"->"一恒","t214_tf"->"[300 TO *]")  fq
    * @param filterFieldsValues  facet.field and facet.querys  eg: Map(
    *                            "t89_s"=>null,
    *                            "t214_tf"=>List("* TO 100","100 TO 200","200 TO *")
    *                            )
    * @param start eg:0
    * @param rows eg:10
    * @return   FilterAttributeSearchResult
    */
  def attributeFilterSearch(keyWords: java.lang.String, catagoryId: java.lang.String, cityId: java.lang.Integer, sorts: java.util.Map[java.lang.String, java.lang.String], filters: java.util.Map[java.lang.String, java.lang.String], filterFieldsValues: java.util.Map[java.lang.String, java.util.List[java.lang.String]], start: java.lang.Integer, rows: java.lang.Integer): FilterAttributeSearchResult = {

    null
  }

  /**
    *
    * @param catagoryId
    * @param sorts   eg:Map(price->desc,sales->desc,score->desc)
    * @param start eg:0
    * @param rows eg:10
    * @return java.util.List[Brand]
    */
  def searchBrandsByCatoryId(catagoryId: java.lang.String, sorts: java.util.Map[java.lang.String, java.lang.String], start: java.lang.Integer, rows: java.lang.Integer): java.util.List[Brand] = {
    null
  }


  /**
    *
    * this for autoSuggest in search
    * @param keyWords search keyword
    * @return  java.util.Map[java.lang.String,java.lang.Integer]   eg:Map("soledede"=>10004)  represent counts of document  the keywords  side in
    */
  def suggestByKeyWords(keyWords: java.lang.String): java.util.Map[java.lang.String, java.lang.Integer] = {
    null

  }


}

object testSearchInterface {
  def main(args: Array[String]) {
    val sorts = new java.util.HashMap[java.lang.String, java.lang.String]
    sorts.put("price", "asc")
    sorts.put("score", "desc")
    SearchInterface.searchByKeywords("防护口罩", 456, sorts, 0, 10)
  }
}
