package search.solr.client.searchInterface

import java.util
import java.util.concurrent.LinkedBlockingQueue

import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrQuery.ORDER
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.util.SimpleOrderedMap
import search.solr.client.config.Configuration
import search.solr.client.entity.searchinterface._
import search.solr.client.log.SearchLog
import search.solr.client.util.{Util, Logging}
import search.solr.client.{SolrClientConf, SolrClient}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.control.Breaks._

/**
  * Created by soledede on 2016/2/20.
  */
object SearchInterface extends Logging with Configuration {

  val spellcheckSeparator = "_____"


  val solrClient = SolrClient(new SolrClientConf())

  val mongoSearchLog = SearchLog("mongo")

  val filterSplitArray = Array("<->", "OR", "or", "->")

  val logQueue = new LinkedBlockingQueue[java.util.Map[String, Object]]


  val generalFacetFieldCategory = "da_2955_s"
  //category facet Map(da_2955_s->类别)
  val generalFacetFieldBrandId = "brandId" //brand facet  Map(brandId->品牌)


  private def groupBucket(q: String, fq: String, json_facet: String): java.util.List[SimpleOrderedMap[java.lang.Object]] = {
    if (json_facet == null) return null
    val query: SolrQuery = new SolrQuery
    query.set("qt", "/select")
    if (q != null && !q.trim.equalsIgnoreCase("")) query.setQuery(q) else query.setQuery("*:*")
    if (fq != null && !fq.trim.equalsIgnoreCase(""))
      query.set("fq", fq)
    query.setParam("json.facet", json_facet)
    query.setRows(0)
    query.setStart(0)
    val rt = solrClient.searchByQuery(query, defaultCollection)
    if (rt == null) return null
    else {
      val r = rt.asInstanceOf[QueryResponse]
      val fMap = r.getResponse
      if (fMap != null) {
        val facetsMap = fMap.get("facets").asInstanceOf[SimpleOrderedMap[java.lang.Object]]
        if (facetsMap != null && !facetsMap.isEmpty && facetsMap.size() > 0) {
          val count = facetsMap.get("count").toString.toInt
          if (count > 0) {
            val catagoryMap = facetsMap.get("categories").asInstanceOf[SimpleOrderedMap[java.util.List[SimpleOrderedMap[java.lang.Object]]]]
            val bucketList = catagoryMap.get("buckets")
            bucketList
          } else null
        } else null
      } else null
    }
  }


  /**
    * get category ids
    *
    * @param keyWords
    * @param cityId
    * @param field
    * @return
    */
  private def getCategoryIds(keyWords: java.lang.String, cityId: java.lang.Integer, field: String): util.List[Integer] = {
    var keyWordsModel = "*:*"
    if (keyWords != null) {
      val keyWord = keyWords.trim.toLowerCase
      keyWordsModel = s"(original:$keyWord^50) OR (sku:$keyWord^50) OR (brandZh:$keyWord^200) OR (brandEn:$keyWord^200) OR (sku:*$keyWord*^11) OR (original:*$keyWord*^10) OR (text:$keyWord^2) OR (pinyin:$keyWord^0.002)"
    }

    val fq = s"isRestrictedArea:0 OR cityId:$cityId"

    var jsonFacet = s"{categories:{type:terms,field:$field,limit:100,sort:{count:desc}}}"
    jsonFacet = jsonFacet.replaceAll(":", "\\:")

    val categoryResultMap = groupBucket(keyWordsModel, fq, jsonFacet)
    var categoryIds: util.List[Integer] = null
    if (categoryResultMap != null) {
      categoryIds = new util.ArrayList[Integer]()
      val categoryResult = categoryResultMap.foreach { kv =>
        val categoryId = kv.get("val").toString.trim.toInt
        categoryIds.add(categoryId)
        //val count = kv.get("count").asInstanceOf[Int]
      }
    }
    categoryIds
  }

  /**
    *
    * search by keywords,must record searchlog for log analysis
    *
    * @param keyWords eg:螺丝钉
    * @param cityId   eg:111
    * @param sorts    eg:Map(price->desc,sales->desc,score->desc)
    * @param start    eg:0
    * @param rows     eg:10
    * @return SearchResult
    */
  def searchByKeywords(keyWords: java.lang.String, cityId: java.lang.Integer, sorts: java.util.Map[java.lang.String, java.lang.String], start: java.lang.Integer, rows: java.lang.Integer): FilterAttributeSearchResult = {
    logInfo(s"search by keywords:$keyWords")

    var filterAttributeSearchResult: FilterAttributeSearchResult = null
    var field = "categoryId4"
    var categoryIds: util.List[Integer] = getCategoryIds(keyWords, cityId, field)
    if (categoryIds != null && categoryIds.size() > 0) {
      val categoryId = categoryIds.get(0)
      filterAttributeSearchResult = searchFilterAttributeAndResulAndSearchResulttByCatagoryIdAndKeywords(categoryId, cityId, keyWords, sorts, start, rows, categoryIds)
      if (filterAttributeSearchResult != null && filterAttributeSearchResult.getFilterAttributes == null) {
        field = "categoryId3"
        filterAttributeSearchResult = searchFilterAttributeAndResulAndSearchResulttByCatagoryIdAndKeywords(categoryId, cityId, keyWords, sorts, start, rows, categoryIds)
      }
    }
    filterAttributeSearchResult
  }


  /**
    *
    * search by keywords,must record searchlog for log analysis
    *
    * @param keyWords eg:螺丝钉
    * @param cityId   eg:111
    * @param sorts    eg:Map(price->desc,sales->desc,score->desc)
    * @param start    eg:0
    * @param rows     eg:10
    * @return SearchResult
    */
  def queryByKeywords(keyWords: java.lang.String, cityId: java.lang.Integer, sorts: java.util.Map[java.lang.String, java.lang.String], start: java.lang.Integer, rows: java.lang.Integer): SearchResult = {
    logInfo(s"search by keywords:$keyWords")
    val msg = new Msg()
    val searchResult = new SearchResult()
    /* if (keyWords == null && cityId == null) {
       msg.setMsg("keyWords and cityId not null")
       searchResult.setMsg(msg)
       return searchResult
     }*/
    /*if (keyWords == null) {
      msg.setMsg("keyWords not null")
      searchResult.setMsg(msg)
      return searchResult
    }*/
    if (cityId == null) {
      msg.setMsg("cityId not null")
      searchResult.setMsg(msg)
      return searchResult
    }

    //page
    var sStart: Int = 0
    var sRows: Int = 10

    if (start != null && start > 0) sStart = start
    if (rows != null && rows > 0) sRows = rows

    var keyWordsModel = "*:*"
    if (keyWords != null) {
      val keyWord = keyWords.trim.toLowerCase
      keyWordsModel = s"(original:$keyWord^50) OR (sku:$keyWord^50) OR (brandZh:$keyWord^200) OR (brandEn:$keyWord^200) OR (sku:*$keyWord*^11) OR (original:*$keyWord*^10) OR (text:$keyWord^2) OR (pinyin:$keyWord^0.002)"
    }

    val fq = s"isRestrictedArea:0 OR cityId:$cityId"


    val query: SolrQuery = new SolrQuery
    query.set("qt", "/select")
    query.setQuery(keyWordsModel)
    query.setFilterQueries(fq)

    //sort
    query.addSort("score", SolrQuery.ORDER.desc)

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

    val r = solrClient.searchByQuery(query, defaultCollection)
    var result: QueryResponse = null
    if (r != null) result = r.asInstanceOf[QueryResponse]

    val resultSearch = getSearchResult(result, searchResult) //get response result
    if (resultSearch != null && resultSearch.size > 0) searchResult.setResult(resultSearch) //set response resut
    searchResult
  }

  /**
    *
    * search by popularity
    *
    * @return
    */
  def popularityKeyWords(): java.util.List[String] = {

    val list = new util.ArrayList[String]()
    list.add("3m")
    list.add("工具箱")
    list.add("安全防护")
    list.add("omron")
    list.add("铅柜")
    list.add("20M胶带价格")
    list.add("西玛特")
    list.add("50ml玻璃试剂瓶")
    list.add("HCN探测器")
    list
  }

  /**
    *
    * @param categoryId
    * @param cityId
    * @param sorts eg:Map(price->desc,sales->desc,score->desc)
    * @param start eg:0
    * @param rows  eg:10
    * @return SearchResult
    */
  def searchByCategoryId(categoryId: java.lang.Integer, cityId: java.lang.Integer, sorts: java.util.Map[java.lang.String, java.lang.String], start: java.lang.Integer, rows: java.lang.Integer): SearchResult = {

    if (categoryId != null) {
      val msg = new Msg()
      val searchResult = new SearchResult()
      /* if (keyWords == null && cityId == null) {
         msg.setMsg("keyWords and cityId not null")
         searchResult.setMsg(msg)
         return searchResult
       }*/
      /*if (keyWords == null) {
        msg.setMsg("keyWords not null")
        searchResult.setMsg(msg)
        return searchResult
      }*/
      if (cityId == null) {
        msg.setMsg("cityId not null")
        searchResult.setMsg(msg)
        return searchResult
      }

      //page
      var sStart: Int = 0
      var sRows: Int = 10

      if (start != null && start > 0) sStart = start
      if (rows != null && rows > 0) sRows = rows


      val keyWordsModel = "*:*"

      val fqGeneral = s"(isRestrictedArea:0 OR cityId:$cityId)"
      val fq = s"(categoryId1:$categoryId OR categoryId2:$categoryId OR categoryId3:$categoryId OR categoryId4:$categoryId)"


      val query: SolrQuery = new SolrQuery
      query.set("qt", "/select")
      query.setQuery(keyWordsModel)
      query.addFilterQuery(fqGeneral)
      query.addFilterQuery(fq)

      //sort
      query.addSort("score", SolrQuery.ORDER.desc)
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

      val r = solrClient.searchByQuery(query, defaultCollection)
      var result: QueryResponse = null
      if (r != null) result = r.asInstanceOf[QueryResponse]

      val resultSearch = getSearchResult(result, searchResult) //get response result
      if (resultSearch != null && resultSearch.size > 0) searchResult.setResult(resultSearch) //set response resut
      searchResult
    } else null
  }


  /**
    *
    * get result spellcheck highlightini once
    *
    * @param msg
    * @param searchResult
    * @param result
    */
  private def getSearchResultByResponse(msg: Msg, searchResult: SearchResult, result: QueryResponse): Unit = {
    val resultSearch = getSearchResult(result, searchResult) //get response result
    if (resultSearch != null && resultSearch.size > 0) searchResult.setResult(resultSearch) //set response resut

    //  highlighting
    val highlighting = getHighlightingList(result)
    if (highlighting != null && highlighting.size() > 0) {
      val filterHighlightins = highlighting.filter(!_._2.isEmpty)
      if (filterHighlightins != null && !filterHighlightins.isEmpty && filterHighlightins.size > 0)
        searchResult.setHighlighting(filterHighlightins)
    }

    //spellcheck
    val spellchecks = getSpellCheckList(result)

    if (spellchecks != null && spellchecks.size() > 0) searchResult.setSpellChecks(spellchecks)

    msg.setMsg("success!")
    msg.setCode(0)
    searchResult.setMsg(msg)
  }

  /**
    *
    * get spellcheck list
    *
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
    *
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
    *
    * @param result
    * @return
    */
  private def getSearchResult(result: QueryResponse, searchResult: SearchResult): java.util.List[util.Map[java.lang.String, Object]] = {
    val resultList: java.util.List[util.Map[java.lang.String, Object]] = new java.util.ArrayList[util.Map[java.lang.String, Object]]() //search result
    //get Result
    if (result != null) {
      // println("params:"+result.getHeader.get("params"))
      val response = result.getResults
      if (response != null) {
        if (searchResult != null) {
          val totalNum = response.getNumFound
          searchResult.setTotal(totalNum.toInt)
        }
        response.foreach { doc =>
          val resultMap: java.util.Map[java.lang.String, Object] = new java.util.HashMap[java.lang.String, Object]()
          val fields = doc.getFieldNames
          fields.foreach { fieldName =>
            resultMap.put(fieldName, doc.getFieldValue(fieldName))
          }
          if (!resultMap.isEmpty)
            resultList.add(resultMap)
        }
      }
    }
    resultList
  }


  /**
    *
    * search keywords log record
    * who where when what
    *
    * @param keyWords
    * @param appKey
    * @param clientIp
    * @param userAgent
    * @param sourceType
    * @param cookies
    * @param userId
    *
    */
  def recordSearchLog(keyWords: java.lang.String, appKey: java.lang.String, clientIp: java.lang.String, userAgent: java.lang.String, sourceType: java.lang.String, cookies: java.lang.String, userId: java.lang.String): Unit = {
    val currentTime = System.currentTimeMillis()
    logInfo(s"record search log:keyWords:$keyWords-appKey:$appKey-clientIp:$clientIp-userAgent:$userAgent-sourceType:$sourceType-cookies:-$cookies-userId:$userId-currentTime:$currentTime")
    val map = new util.HashMap[String, Object]()
    map.put("keyWords", keyWords)
    map.put("appKey", appKey)
    map.put("clientIp", clientIp)
    map.put("userAgent", userAgent)
    map.put("sourceType", sourceType)
    map.put("cookies", cookies)
    map.put("userId", userId)
    map.put("currentTime", currentTime.toString)
    logQueue.put(map)
    //mongoSearchLog.write(keyWords, appKey, clientIp, userAgent, sourceType, cookies, userId, Util.timestampToDate(currentTime))
  }

  private var thread = new Thread("search log thread ") {
    setDaemon(true)

    override def run() {
      while (true) {
        mongoSearchLog.write(logQueue.take())
      }
    }
  }

  thread.start()

  /**
    *
    * get filter atrtributes by catagoryid
    *
    * @param catagoryId
    * @param cityId
    * @return FilterAttribute
    */
  def searchFilterAttributeByCatagoryId(catagoryId: java.lang.Integer, cityId: java.lang.Integer): java.util.List[FilterAttribute] = {
    if (catagoryId != null && cityId != null) {
      val q = s"catid_s:$catagoryId"

      val fl = "filterId_s,attDescZh_s,range_s"

      val query: SolrQuery = new SolrQuery
      query.set("qt", "/select")
      query.setQuery(q)


      query.setFields(fl)

      query.addSort("attSort_ti", SolrQuery.ORDER.desc) //sort

      val r = solrClient.searchByQuery(query, defaultAttrCollection)
      var result: QueryResponse = null
      if (r != null) result = r.asInstanceOf[QueryResponse]
      val resultSearch = getSearchResult(result, null) //get response result

      var filterAttributeSearchResult: FilterAttributeSearchResult = null


      if (resultSearch != null) {
        val filterFieldsValues = new util.HashMap[java.lang.String, util.List[java.lang.String]]()
        resultSearch.foreach { doc =>
          val attributeId = doc.get("filterId_s").toString
          val attributeName = doc.get("attDescZh_s").toString
          setAttributeNameById(attributeId, attributeName) //set cache

          //add facet and facet.query
          val ranges = doc.get("range_s")
          if (ranges != null && !ranges.toString.trim.equalsIgnoreCase("")) {
            //add facet query
            val rangesArray = ranges.toString.split("\\|")
            val rangeList = new util.ArrayList[String]()
            var count: Int = 0
            if (rangesArray.size > 0 && !rangesArray(0).trim.equalsIgnoreCase("") && !rangesArray(0).trim.equalsIgnoreCase("\"\"")) {
              rangesArray.foreach { query =>
                if (count == 0) {
                  val lU = query.split("-")
                  val minV = lU(0).trim
                  rangeList.add(s"[* TO ${minV}}")

                } else if (count == rangesArray.length - 1) {
                  val lU = query.split("-")
                  val maxV = lU(1).trim
                  rangeList.add(s"[${maxV} TO *}")
                }
                val rangeQ = query.replaceAll("-", " TO ")
                rangeList.add(s"[${rangeQ.trim}}")
                count += 1
              }
            }
            if (rangeList.size() > 0)
              filterFieldsValues.put(attributeId, rangeList)
            else filterFieldsValues.put(attributeId, null)

          } else {
            //just facet.field
            filterFieldsValues.put(attributeId, null)
          }
        }

        filterAttributeSearchResult = attributeFilterSearch(null, catagoryId, cityId, null, null, filterFieldsValues, null, null)
      }

      if (filterAttributeSearchResult == null) return null
      else return filterAttributeSearchResult.getFilterAttributes

    } else null
  }


  /**
    *
    * @param catagoryId
    * @param cityId
    * @return FilterAttributeSearchResult
    */
  private def searchFilterAttributeAndResultByCatagoryId(catagoryId: java.lang.Integer, cityId: java.lang.Integer): FilterAttributeSearchResult = {
    if (catagoryId != null && cityId != null) {
      val q = s"catid_s:$catagoryId"

      val fl = "filterId_s,attDescZh_s,range_s"

      val query: SolrQuery = new SolrQuery
      query.set("qt", "/select")
      query.setQuery(q)


      query.setFields(fl)

      query.addSort("attSort_ti", SolrQuery.ORDER.desc) //sort

      val r = solrClient.searchByQuery(query, defaultAttrCollection)
      var result: QueryResponse = null
      if (r != null) result = r.asInstanceOf[QueryResponse]
      val resultSearch = getSearchResult(result, null) //get response result

      var filterAttributeSearchResult: FilterAttributeSearchResult = null


      if (resultSearch != null) {
        val filterFieldsValues = new util.HashMap[java.lang.String, util.List[java.lang.String]]()
        resultSearch.foreach { doc =>
          val attributeId = doc.get("filterId_s").toString
          val attributeName = doc.get("attDescZh_s").toString
          setAttributeNameById(attributeId, attributeName) //set cache

          //add facet and facet.query
          val ranges = doc.get("range_s")
          if (ranges != null && !ranges.toString.trim.equalsIgnoreCase("")) {
            //add facet query
            val rangesArray = ranges.toString.split("\\|")
            val rangeList = new util.ArrayList[String]()
            var count: Int = 0
            if (rangesArray.size > 0 && !rangesArray(0).trim.equalsIgnoreCase("") && !rangesArray(0).trim.equalsIgnoreCase("\"\"")) {
              rangesArray.foreach { query =>
                if (count == 0) {
                  val lU = query.split("-")
                  val minV = lU(0).trim
                  rangeList.add(s"[* TO ${minV}}")

                } else if (count == rangesArray.length - 1) {
                  val lU = query.split("-")
                  val maxV = lU(1).trim
                  rangeList.add(s"[${maxV} TO *}")
                }
                val rangeQ = query.replaceAll("-", " TO ")
                rangeList.add(s"[${rangeQ.trim}}")
                count += 1
              }
            }
            if (rangeList.size() > 0)
              filterFieldsValues.put(attributeId, rangeList)
            else filterFieldsValues.put(attributeId, null)

          } else {
            //just facet.field
            filterFieldsValues.put(attributeId, null)
          }
        }

        filterAttributeSearchResult = attributeFilterSearch(null, catagoryId, cityId, null, null, filterFieldsValues, null, null, null, false)
      }

      if (filterAttributeSearchResult == null) return null
      else return filterAttributeSearchResult

    } else null
  }


  /**
    *
    * @param catagoryId
    * @param cityId
    * @param keywords
    * @return
    */
  private def searchFilterAttributeAndResulAndSearchResulttByCatagoryIdAndKeywords(catagoryId: java.lang.Integer, cityId: java.lang.Integer, keywords: String, sorts: java.util.Map[java.lang.String, java.lang.String], start: java.lang.Integer, rows: java.lang.Integer, categoryIds: java.util.List[Integer] = null): FilterAttributeSearchResult = {
    if (catagoryId != null && cityId != null) {
      val q = s"catid_s:$catagoryId"

      val fl = "filterId_s,attDescZh_s,range_s"

      val query: SolrQuery = new SolrQuery
      query.set("qt", "/select")
      query.setQuery(q)


      query.setFields(fl)

      query.addSort("attSort_ti", SolrQuery.ORDER.desc) //sort

      val r = solrClient.searchByQuery(query, defaultAttrCollection)
      var result: QueryResponse = null
      if (r != null) result = r.asInstanceOf[QueryResponse]
      val resultSearch = getSearchResult(result, null) //get response result

      var filterAttributeSearchResult: FilterAttributeSearchResult = null


      if (resultSearch != null) {
        val filterFieldsValues = new util.HashMap[java.lang.String, util.List[java.lang.String]]()
        resultSearch.foreach { doc =>
          val attributeId = doc.get("filterId_s").toString
          val attributeName = doc.get("attDescZh_s").toString
          setAttributeNameById(attributeId, attributeName) //set cache

          //add facet and facet.query
          val ranges = doc.get("range_s")
          if (ranges != null && !ranges.toString.trim.equalsIgnoreCase("")) {
            //add facet query
            val rangesArray = ranges.toString.split("\\|")
            val rangeList = new util.ArrayList[String]()
            var count: Int = 0
            if (rangesArray.size > 0 && !rangesArray(0).trim.equalsIgnoreCase("") && !rangesArray(0).trim.equalsIgnoreCase("\"\"")) {
              rangesArray.foreach { query =>
                if (count == 0) {
                  val lU = query.split("-")
                  val minV = lU(0).trim
                  rangeList.add(s"[* TO ${minV}}")

                } else if (count == rangesArray.length - 1) {
                  val lU = query.split("-")
                  val maxV = lU(1).trim
                  rangeList.add(s"[${maxV} TO *}")
                }
                val rangeQ = query.replaceAll("-", " TO ")
                rangeList.add(s"[${rangeQ.trim}}")
                count += 1
              }
            }
            if (rangeList.size() > 0)
              filterFieldsValues.put(attributeId, rangeList)
            else filterFieldsValues.put(attributeId, null)

          } else {
            //just facet.field
            filterFieldsValues.put(attributeId, null)
          }
        }

        filterAttributeSearchResult = attributeFilterSearch(keywords, catagoryId, cityId, sorts, null, filterFieldsValues, start, rows, categoryIds, true)
      }

      if (filterAttributeSearchResult == null) return null
      else return filterAttributeSearchResult

    } else null
  }


  /**
    * just category
    *
    * @param catagoryId
    * @param cityId
    * @return
    */
  def attributeFilterSearch(catagoryId: java.lang.Integer, cityId: java.lang.Integer): FilterAttributeSearchResult = {
    searchFilterAttributeAndResultByCatagoryId(catagoryId, cityId)
  }

  /**
    * merge category and filtersearch
    *
    * @param keyWords
    * @param catagoryId
    * @param cityId
    * @param sorts
    * @param filters
    * @param filterFieldsValues
    * @param start
    * @param rows
    * @param isCategoryTouch whether come from category
    * @return FilterAttributeSearchResult
    */
  def attributeFilterSearch(keyWords: java.lang.String, catagoryId: java.lang.Integer, cityId: java.lang.Integer, sorts: java.util.Map[java.lang.String, java.lang.String], filters: java.util.Map[java.lang.String, java.lang.String], filterFieldsValues: java.util.Map[java.lang.String, java.util.List[java.lang.String]], start: java.lang.Integer, rows: java.lang.Integer, isCategoryTouch: java.lang.Boolean): FilterAttributeSearchResult = {
    if (isCategoryTouch) searchFilterAttributeAndResultByCatagoryId(catagoryId, cityId)
    else attributeFilterSearch(keyWords, catagoryId, cityId, sorts, filters, filterFieldsValues, start, rows, null, false)
  }


  /**
    *
    * Tips: front should keep the attributeName cache by searchFilterAttributeByCatagoryId
    *
    * @param keyWords
    * @param catagoryId
    * @param cityId
    * @param sorts              eg:Map(price->desc,sales->desc,score->desc)
    * @param filters            eg:Map("t89_s"->"一恒","t214_tf"->"[300 TO *]")  fq
    * @param filterFieldsValues facet.field and facet.querys  eg: Map(
    *                           "t89_s"=>null,
    *                           "t214_tf"=>List("* TO 100","100 TO 200","200 TO *")
    *                           )
    * @param start              eg:0
    * @param rows               eg:10
    * @return FilterAttributeSearchResult
    */

  def attributeFilterSearch(keyWords: java.lang.String, catagoryId: java.lang.Integer, cityId: java.lang.Integer, sorts: java.util.Map[java.lang.String, java.lang.String], filters: java.util.Map[java.lang.String, java.lang.String], filterFieldsValues: java.util.Map[java.lang.String, java.util.List[java.lang.String]], start: java.lang.Integer, rows: java.lang.Integer, categoryIds: java.util.List[Integer] = null, isComeFromSearch: Boolean = false): FilterAttributeSearchResult = {
    if (cityId != null) {
      val filterAttributeSearchResult = new FilterAttributeSearchResult()

      if (categoryIds != null && categoryIds.size() > 0) filterAttributeSearchResult.setCategoryIds(categoryIds)

      val msg = new Msg()
      val searchResult = new SearchResult()
      //page
      var sStart: Int = 0
      var sRows: Int = 10

      if (start != null && start > 0) sStart = start
      if (rows != null && rows > 0) sRows = rows





      var keyWord: String = null
      if (keyWords != null && !keyWords.trim.equalsIgnoreCase(""))
        keyWord = keyWords.trim.toLowerCase
      var keyWordsModel = "*:*"
      if (keyWord != null)
        keyWordsModel = s"(original:$keyWord^50) OR (sku:$keyWord^50) OR (brandZh:$keyWord^200) OR (brandEn:$keyWord^200) OR (sku:*$keyWord*^11) OR (original:*$keyWord*^10) OR (text:$keyWord^2) OR (pinyin:$keyWord^0.002)"

      val fqGeneral = s"(isRestrictedArea:0 OR cityId:$cityId)"
      val fqCataId = s"(categoryId1:$catagoryId OR categoryId2:$catagoryId OR categoryId3:$catagoryId OR categoryId4:$catagoryId)"

      val query: SolrQuery = new SolrQuery
      query.set("qt", "/select")
      query.setQuery(keyWordsModel)

      query.addFilterQuery(fqGeneral)

      if (!isComeFromSearch) //whether is come from category filter
        query.addFilterQuery(fqCataId)




      if (filters != null && filters.size() > 0) {
        filters.foreach { fV =>
          val field = fV._1
          val value = fV._2
          if (value != null && !value.equalsIgnoreCase("")) {
            var valuesArray: Array[String] = null
            breakable {
              for (i <- 0 to filterSplitArray.length - 1) {
                valuesArray = value.split(filterSplitArray(i).trim)
                if (valuesArray.length > 1) break
              }
            }

            if (valuesArray != null && valuesArray.length > 1) {
              val fqString = new StringBuilder()
              fqString.append("(")
              //t89_s:(memmert+OR+Memmert+OR+honeywell+OR+Honeywell)
              valuesArray.foreach { filterValue =>
                val value = filterValue.trim
                //fq=t89_s:(memmert OR Memmert OR honeywell OR Honeywell)
                if (Util.regex(value, "^[A-Za-z]+$")) {
                  val v1 = value.charAt(0).toUpper + value.substring(1)
                  val v2 = value.charAt(0).toLower + value.substring(1)
                  fqString.append(s"$v1 OR $v2 OR ")
                  // val fq = s"$field:($v1 OR $v2)"
                  //query.addFilterQuery(fq)
                } else {
                  fqString.append(s"$value OR ")
                  // query.addFilterQuery(s"$field:$value")
                }
              }
              val fq = fqString.substring(0, fqString.lastIndexOf("OR") - 1) + ")"
              query.addFilterQuery(s"$field:$fq")
            } else {
              //fq=t89_s:(memmert OR Memmert OR honeywell OR Honeywell)
              if (Util.regex(value, "^[A-Za-z]+$")) {
                val v1 = value.charAt(0).toUpper + value.substring(1)
                val v2 = value.charAt(0).toLower + value.substring(1)
                val fq = s"$field:($v1 OR $v2)"
                query.addFilterQuery(fq)
              } else {
                query.addFilterQuery(s"$field:$value")
              }
            }


          }
        }
      }


      //sort
      query.addSort("score", SolrQuery.ORDER.desc)
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


      //facet and facet query
      query.setFacet(true)
      query.setFacetMinCount(1)
      query.setFacetMissing(false)


      //facet for category and brandId
      query.addFacetField(generalFacetFieldCategory)
      query.addFacetField(generalFacetFieldBrandId)

      if (filterFieldsValues != null && filterFieldsValues.size() > 0) {
        filterFieldsValues.foreach { facet =>
          val field = facet._1
          val ranges = facet._2
          if (ranges != null && ranges.size() > 0) {
            //range facet.query
            ranges.foreach(range => query.addFacetQuery(s"$field:$range"))
          } else {
            //facet.field
            query.addFacetField(field)
          }
        }
      }


      //page
      query.setStart(sStart)
      query.setRows(sRows)




      val r = solrClient.searchByQuery(query, defaultCollection)
      var result: QueryResponse = null
      if (r != null) result = r.asInstanceOf[QueryResponse]
      getSearchResultByResponse(msg, searchResult, result) //get searchResult

      filterAttributeSearchResult.setSearchResult(searchResult) //set searchResult


      getFacetFieldAndFacetQueryToFilterAttributes(filterAttributeSearchResult, result)
      filterAttributeSearchResult
    } else null
  }

  private def getFacetFieldAndFacetQueryToFilterAttributes(filterAttributeSearchResult: FilterAttributeSearchResult, result: QueryResponse): Unit = {
    if (result != null) {
      val filterAttributes = new util.ArrayList[FilterAttribute]()

      val facetFields = result.getFacetFields
      if (facetFields != null && facetFields.size() > 0) {
        //facet.field
        facetFields.foreach { facetField =>
          val filterAttribute = new FilterAttribute()

          val facetFieldName = facetField.getName
          val facetFieldValues = facetField.getValues
          filterAttribute.setAttrId(facetFieldName)
          filterAttribute.setAttrName(getAttributeNameById(facetFieldName))

          if (facetFieldValues != null && facetFieldValues.size() > 0) {
            val attributeCountMap = new util.HashMap[String, Integer]()
            facetFieldValues.foreach { facetcount =>
              val attributeValue = facetcount.getName
              val count = facetcount.getCount.toInt
              attributeCountMap.put(attributeValue, count)
            }
            filterAttribute.setAttrValues(attributeCountMap)
            filterAttribute.setRangeValue(false)
            filterAttributes.add(filterAttribute)
          }
        }
      }

      //facet.query
      val facetQuerys = result.getFacetQuery

      if (facetQuerys != null && !facetQuerys.isEmpty) {
        //facet.query
        /**
          * "t87_tf:[* TO 0}":0,
          * "t87_tf:[0 TO 10}":0,
          * "t87_tf:[10 TO 20}":1,
          * "t87_tf:[20 TO 30}":2,
          * "t87_tf:[30 TO *}":4},
          */
        val facetQueryCountMap = new util.HashMap[String, util.Map[String, Integer]]()

        facetQuerys.foreach { facetQuery =>
          val query = facetQuery._1
          val count = facetQuery._2
          if (count > 0) {
            val queryFields = query.split(":")
            val field = queryFields(0)
            val attrValue = queryFields(1)
            if (!facetQueryCountMap.contains(field.trim)) {
              val countMap = new util.HashMap[String, Integer]()
              countMap.put(attrValue, count)
              facetQueryCountMap.put(field.trim, countMap)
            } else {
              facetQueryCountMap.get(field.trim).put(attrValue, count)
            }
          }

        }
        if (!facetQueryCountMap.isEmpty) {
          facetQueryCountMap.foreach { facetQuery =>
            val attributeId = facetQuery._1
            val attributeName = getAttributeNameById(attributeId)
            val attributeValues = facetQuery._2
            val isRangeValue = true
            val filterAttribute = new FilterAttribute(attributeId, attributeName, attributeValues, isRangeValue)
            filterAttributes.add(filterAttribute)
          }

        }

      }


      if (filterAttributes.size() != 0) filterAttributeSearchResult.setFilterAttributes(filterAttributes)
    }
  }


  /**
    *
    * get all brands by catoryId
    *
    * @param catagoryId
    * @param cityId
    * @return
    */
  def searchBrandsByCatoryId(catagoryId: java.lang.Integer, cityId: java.lang.Integer): java.util.List[Brand] = {
    searchBrandsByCatoryId(catagoryId, cityId, null, 0, java.lang.Integer.MAX_VALUE)
  }

  /**
    *
    * @param catagoryId
    * @param cityId
    * @param sorts eg:Map(price->desc,sales->desc,score->desc)
    * @param start eg:0
    * @param rows  eg:10
    * @return java.util.List[Brand]
    */
  def searchBrandsByCatoryId(catagoryId: java.lang.Integer, cityId: java.lang.Integer, sorts: java.util.Map[java.lang.String, java.lang.String], start: java.lang.Integer, rows: java.lang.Integer): java.util.List[Brand] = {

    if (catagoryId != null) {
      //page
      var sStart: Int = 0
      var sRows: Int = 10

      if (start != null && start > 0) sStart = start
      if (rows != null && rows > 0) sRows = rows


      val keyWordsModel = "*:*"

      val fqGeneral = s"(isRestrictedArea:0 OR cityId:$cityId)"
      val fq = s"(categoryId1:$catagoryId OR categoryId2:$catagoryId OR categoryId3:$catagoryId OR categoryId4:$catagoryId)"

      val fl = "brandId,brandEn,brandZh"

      val query: SolrQuery = new SolrQuery
      query.set("qt", "/select")
      query.setQuery(keyWordsModel)
      query.addFilterQuery(fqGeneral)
      query.addFilterQuery(fq)
      query.setFields(fl)

      //sort
      query.addSort("score", SolrQuery.ORDER.desc)
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

      val r = solrClient.searchByQuery(query, defaultCollection)
      var result: QueryResponse = null
      if (r != null) result = r.asInstanceOf[QueryResponse]
      val brandIdToBrandMap = getBrandsSearchResultUniqueById(result)

      if (brandIdToBrandMap != null) {
        val brandList = new java.util.ArrayList[Brand]()
        brandIdToBrandMap.foreach { idToBrand =>
          brandList.add(idToBrand._2)
        }
        if (brandList.size() > 0) brandList
        else null
      } else null
    } else null
  }

  /**
    *
    * get brands    Map(brandId->Brand)
    *
    * @param result
    * @return
    */
  private def getBrandsSearchResultUniqueById(result: QueryResponse): java.util.Map[java.lang.Integer, Brand] = {

    //get Result
    if (result != null) {
      val resultBrandIdMap: java.util.Map[java.lang.Integer, Brand] = new java.util.HashMap[java.lang.Integer, Brand]() //brand result Map(brandId->Brand)
      val response = result.getResults
      if (response != null) {
        response.foreach { doc =>
          val brandId = doc.getFieldValue("brandId").toString.toInt
          if (!resultBrandIdMap.contains(brandId)) {
            val brandEn = doc.getFieldValue("brandEn").toString
            val brandZh = doc.getFieldValue("brandZh").toString
            val brand = new Brand(brandId, brandZh, brandEn)
            resultBrandIdMap.put(brandId, brand)
          }
        }
      }
      if (!resultBrandIdMap.isEmpty) resultBrandIdMap
      else null
    }
    else null
  }

  def countKeywordInDocs(keyword: Object, query: SolrQuery, cityId: java.lang.Integer): Int = {
    if (keyword != null) {
      val keyWord = keyword.toString.trim.toLowerCase
      val keyWordsModel = s"(original:$keyWord^50) OR (sku:$keyWord^50) OR (brandZh:$keyWord^200) OR (brandEn:$keyWord^200) OR (sku:*$keyWord*^11) OR (original:*$keyWord*^10) OR (text:$keyWord^2) OR (pinyin:$keyWord^0.002)"

      val fq = s"isRestrictedArea:0 OR cityId:$cityId"


      query.set("qt", "/select")
      query.setQuery(keyWordsModel)
      if (cityId != null) {
        query.setFilterQueries(fq)
      }
      query.setQuery(keyWordsModel)
      query.setRows(1)
      val r = solrClient.searchByQuery(query, defaultCollection)
      var result: QueryResponse = null
      if (r != null) result = r.asInstanceOf[QueryResponse]
      if (result != null) {
        val resultDocs = result.getResults
        if (resultDocs != null && resultDocs.size() > 0) {
          val numfound = resultDocs.getNumFound
          numfound.toInt
        }
        else 0
      } else 0
    } else 0
  }

  /**
    *
    * this for autoSuggest in search
    *
    * @param keyWords search keyword
    * @param cityId
    * @return java.util.Map[java.lang.String,java.lang.Integer]   eg:Map("soledede"=>10004)  represent counts of document  the keywords  side in
    */
  def suggestByKeyWords(keyWords: java.lang.String, cityId: java.lang.Integer): java.util.Map[java.lang.String, java.lang.Integer] = {


    if (keyWords != null && !keyWords.trim.equalsIgnoreCase("")) {


      val keyWordsPro = keyWords.trim.toLowerCase
      val keyWordsModel = s"(kw_ik:${keyWordsPro}* OR pinyin:${keyWordsPro}* OR py:${keyWordsPro}*)"

      val fl = "kw"


      val query: SolrQuery = new SolrQuery
      // query.set("qt", "/select")
      query.setQuery(keyWordsModel)

      query.setFields(fl)

      //sort
      query.addSort("weight", SolrQuery.ORDER.desc)

      query.setStart(0)
      query.setRows(20)

      val r = solrClient.searchByQuery(query, defaultSuggestCollection)
      var result: QueryResponse = null
      if (r != null) result = r.asInstanceOf[QueryResponse]
      val docs = getSearchResult(result, null)

      if (docs != null) {
        val keyWordsMap = new java.util.HashMap[java.lang.String, java.lang.Integer]()
        val query: SolrQuery = new SolrQuery
        docs.foreach { doc =>
          val keyword = doc.get("kw").toString.trim
          val count: Int = countKeywordInDocs(keyword, query, cityId)
          if (count > 0) keyWordsMap.put(keyword, count)
        }
        if (!keyWordsMap.isEmpty) keyWordsMap
        else null
      } else null

    }
    else null
  }

  /**
    *
    * data dictionary attribute id to attribute name
    *
    * @param attributeId
    * @return
    */
  def getAttributeNameById(attributeId: String): String = {
    if (attributeId == null || attributeId.trim.equals("")) return null
    if (attributeId.equals(generalFacetFieldCategory)) return "类别"
    if (attributeId.equals(generalFacetFieldBrandId)) return "品牌"
    val attrCache = FilterAttribute.attrIdToattrName
    var attrName: String = null
    if (attrCache.contains(attributeId.trim)) {
      attrName = attrCache.get(attributeId.trim)
    }
    attrName
  }


  /**
    *
    * set data dictionary attribute id to attribute name
    *
    * @param attributeId
    * @param attributeName
    * @return
    */
  private def setAttributeNameById(attributeId: String, attributeName: String): Unit = {
    val attrCache = FilterAttribute.attrIdToattrName
    if (!attrCache.contains(attributeId.trim)) {
      attrCache.put(attributeId.trim, attributeName.trim)
    }
  }


}

object testSearchInterface {
  def main(args: Array[String]) {

    searchByKeywords


    //testSearchFilterAttributeByCatagoryId
    //testAttributeFilterSearch

    //testSearchBrandsByCatoryId

    // testSuggestByKeyWords

    //testRecordSearchLog

    //testCountKeywordInDocs


    // testSplit
    //testRegex
    // testMaxInt
    //testSubString


    //testSearchByCategoryId

  }


  def searchByKeywords = {

    val sorts = new java.util.HashMap[java.lang.String, java.lang.String]
    sorts.put("price", "desc")
    //sorts.put("score", "desc")
    //  val result = SearchInterface.searchByKeywords("防护口罩", 456, sorts, 0, 10)
    //val result = SearchInterface.searchByKeywords("西格玛", 363, null, 0, 10)
    val result = SearchInterface.searchByKeywords("1234567", 363, null, 0, 10)
    val starTime = System.currentTimeMillis()
    val result1 = SearchInterface.searchByKeywords("白板笔", 363, null, 0, 10)
    val endTime = System.currentTimeMillis()
    println(result1)
    println(endTime - starTime)
  }

  def queryByKeywords = {

    val sorts = new java.util.HashMap[java.lang.String, java.lang.String]
    sorts.put("price", "desc")
    //sorts.put("score", "desc")
    //  val result = SearchInterface.searchByKeywords("防护口罩", 456, sorts, 0, 10)
    //val result = SearchInterface.searchByKeywords("西格玛", 363, null, 0, 10)
    val result = SearchInterface.searchByKeywords("优特", 363, null, 0, 10)
    val starTime = System.currentTimeMillis()
    SearchInterface.queryByKeywords("优特", 363, null, 0, 10)
    val endTime = System.currentTimeMillis()
    println(result)
    println(endTime - starTime)
  }

  def testSearchByCategoryId() = {
    val result = SearchInterface.searchByCategoryId(14887, 321, null, 0, 10)
    println(result)

  }

  def testSearchFilterAttributeByCatagoryId() = {
    //val result = SearchInterface.searchFilterAttributeByCatagoryId(1001739, 456)
    // val result = SearchInterface.searchFilterAttributeByCatagoryId(2660, 321)
    val result = SearchInterface.searchFilterAttributeByCatagoryId(1225, 321)
    println(result)
  }

  def testAttributeFilterSearch = {
    //keywords catid cityid sorts filters filterFieldsValues start rows
    val sorts = new java.util.HashMap[java.lang.String, java.lang.String]
    sorts.put("price", "asc")
    sorts.put("score", "desc")

    val filters = new java.util.HashMap[java.lang.String, java.lang.String]()
    //filters.put("t89_s", "Memmert")
    filters.put("t89_s", "Memmert <-> honeywell")
    filters.put("t87_tf", "[0 TO *}")

    val filterFieldsValues = new util.HashMap[java.lang.String, util.List[java.lang.String]]()
    filterFieldsValues.put("t89_s", null)
    val rangeList = new util.ArrayList[String]()
    rangeList.add("[* TO 0}")
    rangeList.add("[0 TO 10}")
    rangeList.add("[10 TO 20}")
    rangeList.add("[20 TO 30}")
    rangeList.add("[30 TO *}")
    filterFieldsValues.put("t87_tf", rangeList)
    //SearchInterface.attributeFilterSearch(null, 1001739, 456, sorts, null, filterFieldsValues, 0, 10)
    // val result = SearchInterface.attributeFilterSearch(null, 1001739, 456, sorts, filters, filterFieldsValues, 0, 10)
    // val result = SearchInterface.attributeFilterSearch("3M", 1001739, 456, sorts, filters, filterFieldsValues, 0, 10)

    val filters1 = new java.util.HashMap[java.lang.String, java.lang.String]()
    //filters.put("t89_s", "Memmert")
    //filters1.put("da_1385_s", "1/4")
    /// filters1.put("da_89_s", "亚德客")
    // filters1.put("da_2306_s", "附锁型")
    //filters1.put("da_2178_s", "二位三通")

    val filterFieldsValues1 = new util.HashMap[java.lang.String, util.List[java.lang.String]]()
    filterFieldsValues1.put("da_1385_s", null)
    filterFieldsValues1.put("da_2178_s", null)
    filterFieldsValues1.put("da_2306_s", null)
    filterFieldsValues1.put("da_89_s", null)


    // val result = SearchInterface.attributeFilterSearch(null, 1225, 321, null, filters1, filterFieldsValues1, 0, 10)

    // val result = SearchInterface.attributeFilterSearch(1225, 321)
    /* val result = SearchInterface.attributeFilterSearch(null,1225, 321,null,null,null,null,null,true)
     val result1 = SearchInterface.attributeFilterSearch(null, 1225, 321, null, filters1, filterFieldsValues1, 0, 10,false)*/

    val result = SearchInterface.attributeFilterSearch(null, 521, 321, null, null, null, null, null, true)
    val result1 = SearchInterface.attributeFilterSearch(null, 521, 321, null, filters1, filterFieldsValues1, 0, 10, false)

    val result2 = SearchInterface.attributeFilterSearch("优特", -1, 321, sorts, null, filterFieldsValues, 0, 10, null,true)
    println(result)
  }

  def testSearchBrandsByCatoryId() = {
    val result = SearchInterface.searchBrandsByCatoryId(2660, 321)
    println(result)
  }

  def testSuggestByKeyWords() = {

    val result = SearchInterface.suggestByKeyWords("dai", 456)
    var startTime = System.currentTimeMillis().toDouble
    SearchInterface.suggestByKeyWords("dai", 456)
    var endTime = System.currentTimeMillis().toDouble
    println(s"cost time:${(endTime - startTime) / 1000}s")

    startTime = System.currentTimeMillis().toDouble
    SearchInterface.suggestByKeyWords("dai", 456)
    endTime = System.currentTimeMillis().toDouble
    println(s"cost time:${(endTime - startTime) / 1000}s")


    startTime = System.currentTimeMillis().toDouble
    SearchInterface.suggestByKeyWords("dai", 456)
    endTime = System.currentTimeMillis().toDouble
    println(s"cost time:${(endTime - startTime) / 1000}s")

    println(result)
  }

  def testRecordSearchLog() = {
    /**
      * keyWords
      * appKey
      * clientIp
      * userAgent
      * sourceType
      * cookies
      * userId
      */
    val startTime = System.currentTimeMillis()
    SearchInterface.recordSearchLog("防护口罩", "swe2323", null, "Useragent", "android", null, "undn3")
    //Thread.sleep(6000)
    SearchInterface.recordSearchLog("防护口罩", "swe2323", null, "Useragent", "android", null, "undn3")
    val endTime = System.currentTimeMillis()
    println(endTime - startTime)
  }

  def testSplit() = {
    //  val testString = "t87_tf:[* TO 0}"
    //  val array = testString.split(":")
    //  println(array)

    val test = "memmert<->Honeywell"
    val tesArray = test.split("<->")
    println(tesArray.toString)

    val testOrString = "memmert OR Honeywell"
    val testOrStringArray = testOrString.split("OR")
    println(testOrStringArray)

    val vls = "_32"
    val valsArray = vls.split("_")
    println(valsArray)


  }

  def testRegex() = {
    // val value = "中Mmemert"
    //val value = "[Mmemert"
    var value = "mmemert"
    if (Util.regex(value, "^[A-Za-z]+$")) println("true") else println("false")

    if (Util.regex(value, "^[A-Za-z]+$")) {
      val v1 = value.charAt(0).toUpper + value.substring(1)
      val v2 = value.charAt(0).toLower + value.substring(1)
      println(v1 + "=" + v2)
    }
  }

  def testCountKeywordInDocs() = {
    SearchInterface.countKeywordInDocs("3m", new SolrQuery(), 456)
  }

  def testMaxInt() = {
    val v = java.lang.Integer.MAX_VALUE
    println(v)
  }

  def testSubString() = {
    val s = new StringBuilder("(memmert OR Memmert OR honeywell OR Honeywell OR ")
    val sS = s.substring(0, s.lastIndexOf("OR") - 1)
    val laS = sS + ")"
    println(laS)

  }


}
