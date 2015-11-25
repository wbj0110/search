package search.solr.client.util

import java.text.SimpleDateFormat

/**
  * Created by soledede on 2015/11/25.
  */
object Util {

  def dateToString(date: java.util.Date) = {
    val format = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss")
    format.format(date)
  }

}
