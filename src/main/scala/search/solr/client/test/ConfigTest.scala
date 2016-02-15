package search.solr.client.test

import search.solr.client.config.Configuration

/**
  * Created by soledede on 2016/2/14.
  */
object ConfigTest extends Configuration{

  def main(args: Array[String]) {
    println("serviceHost="+brokers)
  }

}
