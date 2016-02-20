package search.solr.client.searchInterface

import org.apache.solr.client.solrj.SolrQuery
import search.solr.client.entity.searchinterface.{Brand, FilterAttributeSearchResult, FilterAttribute}
import search.solr.client.util.Logging
import search.solr.client.{SolrClientConf, SolrClient}

import scala.collection.Searching.SearchResult

/**
  * Created by soledede on 2016/2/20.
  */
object SearchInterface extends Logging {
  val solrClient = SolrClient(new SolrClientConf())


  /**
    *
    * search by keywords,must record searchlog for log analysis
    * @param keyWords eg:螺丝钉
    * @param cityId eg:111
    * @param sorts  eg:Map(price->desc,sales->desc,score->desc)
    * @return SearchResult
    */
  def searchByKeywords(keyWords: java.lang.String, cityId: java.lang.Integer, sorts: java.util.Map[java.lang.String, java.lang.String]): SearchResult = {

    val keyWord = keyWords.trim
    val keyWordsModel = s"(original:$keyWord^50) OR (sku:$keyWord^50) OR (brandZh_ps$keyWord^30) OR (brandEn_ps:$keyWord^30) OR (sku:*$keyWord*^11) OR (original:*$keyWord*^10) OR (text:$keyWord^2) OR (pinyin:$keyWord^0.002)"

    val fq = s"deliveryTime:0 OR cityId:$cityId"

    // val sort
    val query: SolrQuery = new SolrQuery
    query.set("qt", "/select")
    query.setQuery(keyWordsModel)
    query.setFilterQueries(fq)
    query.addSort("price", SolrQuery.ORDER.desc)
    val r = solrClient.searchByQuery(query, "mergescloud")
    println(r)
    null
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
    * @param cityId
    * @param sorts   eg:Map(price->desc,sales->desc,score->desc)
    * @param filters eg:Map("t89_s"=>"一恒")
    * @param filterFieldsValues eg: Map(
    *                           "t89_s"=>List(),
    *                           "t214_tf"=>List("* TO 100","100 TO 200","200 TO *")
    *                           )
    * @param allFilterAttribuates
    * @return   FilterAttributeSearchResult
    */
  def attributeFilterSearch(keyWords: java.lang.String, cityId: java.lang.Integer, sorts: java.util.Map[java.lang.String, java.lang.String], filters: java.util.Map[java.lang.String, java.lang.String], filterFieldsValues: java.util.Map[java.lang.String, java.lang.String], allFilterAttribuates: java.util.Map[java.lang.String, java.util.List[java.lang.String]]): FilterAttributeSearchResult = {

    null
  }

  /**
    *
    * @param catagoryId
    * @return java.util.List[Brand]
    */
  def searchBrandsByCatoryId(catagoryId: java.lang.String): java.util.List[Brand] = {
    null
  }


}

object testSearchInterface {
  def main(args: Array[String]) {
    SearchInterface.searchByKeywords("防护口罩", 456, null)
  }
}
