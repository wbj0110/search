package search.solr.client.listener

import search.solr.client.util.Logging

/**
 * Created by soledede on 2016/4/8.
 */
trait TraceListener extends Logging{

  def onJobStart(jobstart: JobStarted) = {}

  def onJobTaskFailed(jobTaskFailed: JobTaskFailed): Int = {-1}

  def onJobTaskCompleted(jobTaskCompleted: JobTaskCompleted): Int = {-1}

  def onJobTaskAdded(jobTaskAdded: JobTaskAdded): Int = {-1}

  def onSearch(keys: Keys): Option[Seq[String]] = {null}
}
