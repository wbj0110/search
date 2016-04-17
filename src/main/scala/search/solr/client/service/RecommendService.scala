package search.solr.client.service


import search.solr.client.entity.RecommendResult

/**
  * Created by soledede on 2015/12/15.
  */
trait RecommendService {
  def recommendByDocId(docId: String, number: Int): Seq[RecommendResult] = null

  def recommendByUserId(userId: String, number: Int): Seq[RecommendResult] = null

  def recommendMostLikeCatagoryIdByKeywords(keywords: String): String = null

  def recommendByCatagoryId(catagoryId: String, number: Int): Seq[RecommendResult] = null

  def recommendByBrandId(brandId: String, number: Int): Seq[RecommendResult] = null
}

object RecommendService {
  def apply(name: String = "solrCF"): RecommendService = {
    name match {
      case "solrCF" =>  null//SolrRecommendCF()
      case "solrCT" => null//SolrRecommendCategory()
      case "moreLikeThis" => null//SolrRecommendMoreLikeThis()
      case _ => null
    }
  }
}
