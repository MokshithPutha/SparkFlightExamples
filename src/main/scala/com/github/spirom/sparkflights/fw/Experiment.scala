package com.github.spirom.sparkflights.fw

import java.text.SimpleDateFormat
import java.util.Calendar

import org.apache.log4j.Logger
import org.apache.spark.sql.DataFrame

abstract class Experiment(val name: String) {

  val logger = Logger.getLogger(getClass.getName)

  def runQuery(df: DataFrame, runOutputBase: String, index: Int): Unit

  def run(df: DataFrame, runOutputBase: String, index: Int, results: Results): Unit = {

    val timeFormat = new SimpleDateFormat("hh:mm:ss")

    val before = Calendar.getInstance().getTime()
    logger.info(s"Running $name at ${timeFormat.format(before)}")

    try {
      runQuery(df, runOutputBase, index)

      val after = Calendar.getInstance().getTime()

      val diff = after.getTime - before.getTime

      val result = new ExperimentResult(this, before, after, diff, None)
      results.add(result)

      logger.info(s"Completed $name at ${timeFormat.format(after)} after $diff msec")
    } catch {
      case (t: Throwable) => {
        val after = Calendar.getInstance().getTime()

        val diff = after.getTime - before.getTime

        val result = new ExperimentResult(this, before, after, diff, Some(t))
        results.add(result)

        logger.warn(s"Failed $name at ${timeFormat.format(after)} after $diff msec", t)
      }
    }
  }
}
