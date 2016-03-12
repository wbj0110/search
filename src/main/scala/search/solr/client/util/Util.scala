package search.solr.client.util

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.concurrent.{ThreadFactory, Executors, ThreadPoolExecutor}
import java.util.regex.{Matcher, Pattern}

import com.google.common.util.concurrent.ThreadFactoryBuilder

import scala.collection.mutable.ListBuffer

/**
  * Created by soledede on 2015/11/25.
  */
object Util {


  private val daemonThreadFactoryBuilder: ThreadFactoryBuilder =
    new ThreadFactoryBuilder().setDaemon(true)

  def dateToString(date: java.util.Date) = {
    val format = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss")
    format.format(date)
  }

  def timestampToDate(timestamp: Long): java.util.Date = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val d = format.format(timestamp)
    val date = format.parse(d)
    date
  }

  def stringTotimestamp(time: String): Long = {
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    //val d = format.format(time)
    val date = format.parse(time)
    date.getTime
  }

  def namedThreadFactory(prefix: String): ThreadFactory = {
    daemonThreadFactoryBuilder.setNameFormat(prefix + "-%d").build()
  }

  def newDaemonFixedThreadPool(nThreads: Int, prefix: String): ThreadPoolExecutor = {
    val threadFactory = namedThreadFactory(prefix)
    Executors.newFixedThreadPool(nThreads, threadFactory).asInstanceOf[ThreadPoolExecutor]
  }

  def inferCores(): Int = {
    Runtime.getRuntime.availableProcessors()
  }

  def regex(input: String, regex: String): Boolean = {
    if (regex == null || input == null) {
      return false
    }
    val p: Pattern = Pattern.compile(regex)
    val m: Matcher = p.matcher(input)

    if (m.find()) return true
    else return false
  }

  def regexExtract(input: String, regex: String): AnyRef = regexExtract(input, regex, -2)

  def regexExtract(input: String, regex: String, group: Int): AnyRef = {
    if (regex == null) {
      return input
    }
    val p: Pattern = Pattern.compile(regex)
    val m: Matcher = p.matcher(input)
    if (group == -2) {
      val list = new ListBuffer[String]

      while (m.find) {
        var i: Int = 1
        while (i <= m.groupCount) {
          {
            list.append(m.group(i))
          }
          i += 1
        }
      }
      return list
    } else {
      if (m.find) {
        if (group == -1) {
          var r = ""
          val s = new StringBuilder
          var i: Int = 0
          while (i <= m.groupCount - 1) {
            {
              s ++= m.group(i)
              if (i != m.groupCount) {
                s ++= " "
              }
            }
            ({
              i += 1
            })
          }
          r = s.toString
          if (r.length == 0)
            r = m.group()
          return r
        }
        else {
          return m.group(group)
        }
      }
      else return ""
    }
  }
}

object testUtil{
  def main(args: Array[String]) {
    testTime
  }

  def testTime = {
    //println( Util.stringTotimestamp("2016-03-07 01:41:39.000"))
   // println(Util.timestampToDate(1457477786924L))
    println(Util.timestampToDate(1457759504865L))
  }
}