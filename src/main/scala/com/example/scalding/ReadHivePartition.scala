package com.example.scalding

import java.io.IOException

import com.github.nscala_time.time.Imports._
import com.twitter.scalding._
import org.apache.hadoop.fs.{FileSystem, Path}

object ReadHivePartition {

  val HIVE_TABLE_SCHEMA = List('id, 'card_id, 'zip_code, 'status, 'novalue, 'nullable, 'col1, 'col2, 'col3, 'col4,
    'col5, 'col6, 'col7, 'col8, 'col9, 'col10, 'col11, 'col12, 'col13, 'col14, 'col15, 'col16, 'col17, 'col18,
    'col19, 'col20)

  val DEFAULT_ARGUMENTS = Array("--hdfs", "--priority", "HIGH", "--reducer", "20",
    "--hive_table", "hdfs://mycluster/hive/hivetable/",
    "--address_output", "hdfs://mycluster/address_tsv/",
    "--tsv_output", "hdfs://mycluster/output/",
    "--today", DateTime.now.toString("yyyyMMdd"))

  def stringColumnToString(string: String): String = {
    string match {
      case null | "\\N" => ""
      case s: String => if (s.isEmpty) "" else s
    }
  }
}

class ReadHivePartition(args: Args) extends Job(args) {
  override def config: Map[AnyRef, AnyRef] = {
    super.config ++ Map("mapred.job.queue.name" -> "MY_QUEUE",
      "mapred.job.priority" -> args.getOrElse("priority", "NORMAL"))
  }

  val hadoopConf = implicitly[Mode] match {
    case Hdfs(_, configuration) =>
      try {
        configuration
      } catch {
        case ioe: IOException =>
          Console.err.println("IOException during operation: " + ioe)
          sys.exit(1)
      }
    case _ =>
      Console.err.println("Not running on Hadoop! (maybe scalding local mode?)")
      sys.exit(1)
  }

  val reducerNum = args.getOrElse("reducer", "20").toInt

  val today = args.getOrElse("today", DateTime.now.toString("yyyyMMdd"))
  val targetDt = DateTimeFormat.forPattern("yyyyMMdd").parseLocalDate(today).minusDays(1).toString("yyyyMMdd")

  val hiveTableDir = args("hive_table") + "/dt=" + targetDt
  val hiveTablePath = new Path(hiveTableDir)
  val fs = FileSystem.get(hadoopConf)
  if (!fs.exists(hiveTablePath) || !fs.getFileStatus(hiveTablePath).isDirectory) {
    Console.err.println(hiveTableDir + "does not exit or is not directory")
    sys.exit(1)
  }

  val zipCodeToDongCode = Tsv(args("address_output"), fields = ZipCodeToAdminCode.CODE_CONVERT_SCHEMA).read
    .project('zip_code, 'admin_code_prefix)

  val hiveTable = Tsv(hiveTableDir, fields = ReadHivePartition.HIVE_TABLE_SCHEMA).read
    .project('id, 'card_id, 'zip_code, 'status, 'novalue, 'nullable)
    .filter('novalue) { novalue: String => null != novalue && !novalue.isEmpty && !novalue.equals("\\N")}
    .map('nullable -> 'nullable) { nullable: String => ReadHivePartition.stringColumnToString(nullable)}
    .insert('dt, targetDt)
    .insert('count, 1)
    .groupBy('id, 'card_id) {
    group => group.sortBy('dt)
      .reducers(reducerNum)
      .last('zip_code, 'status, 'novalue, 'nullable, 'dt)
      .size('size)
      .sum[Int]('count -> 'count)
      .mkString('dt -> 'dt_list, ":")
  }
    .project('id, 'card_id, 'zip_code, 'status, 'novalue, 'nullable, 'dt, 'size, 'count, 'dt_list)
    .rename('zip_code, 'zip_code_temp)

  val syrupTargeting = hiveTable.leftJoinWithTiny('zip_code_temp -> 'zip_code, zipCodeToDongCode)
    .map('admin_code_prefix -> 'admin_code) { adminCode: String =>
    if (null == adminCode || adminCode.length < 8) "" else adminCode.substring(0, 8)
  }
    .project('id, 'card_id, 'zip_code, 'admin_code, 'status, 'novalue, 'nullable, 'dt, 'size, 'count, 'dt_list)
    .write(Tsv(args("tsv_output"), writeHeader = false))
}