package com.github.spirom.sparkflights.fw

import java.text.SimpleDateFormat
import java.util.Calendar

import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame

class Runner(experiments: Iterable[Experiment], sc: SparkContext,
              rddLogger: RDDLogger, unknown: Seq[String] = Seq()) {

  val logger = Logger.getLogger(getClass.getName)

  val results = new Results()

  val runId = makeIdFromDateTime()

  def makeIdFromDateTime(): String = {
    val now = Calendar.getInstance().getTime()
    val idFormat = new SimpleDateFormat("YYYY_MM_dd_HHmmss")
    val id = idFormat.format(now)
    id
  }

  def run(df: DataFrame, outputBase: String) : Unit = {

    val runOutputBase = outputBase + "/" + runId

    logger.info(s"Running all experiments for run $runId")

    logger.info(s"Saving results under $runOutputBase")

    var index = 1
    experiments.foreach(e => {
      rddLogger.log("before " + e.name)
      e.run(df, runOutputBase, index, results)
      index = index + 1
    })

    logger.info(s"Completed all experiments for run $runId")

  }

  def saveSummary(outputBase: String, sc: SparkContext) : Unit = {
    val summaryBase = outputBase + "/" + runId + "/summary"

    results.save(summaryBase, sc)

    sc.parallelize(unknown, 1).saveAsTextFile(s"$summaryBase/unknown")
  }
}
