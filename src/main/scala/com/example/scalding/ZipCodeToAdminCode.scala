package com.example.scalding

import com.twitter.scalding.{Args, Job, TextLine, Tsv}

object ZipCodeToAdminCode {
  val ADDRESS_SCHEMA = ('id, 'admin_code, 'admin_name, 'zip_code, 'zip_seq, 'mass_name, 'list_name, 'building_name,
    'common_yn)
  val CODE_CONVERT_SCHEMA = ('zip_code, 'admin_code_prefix, 'unique_count, 'admin_code_list)

  val DEFAULT_ARGUMENTS = Array("--hdfs", "--priority", "HIGH",
    "--encoding", "cp949",
    "--address_input", "hdfs://mycluster/address_txt/",
    "--address_output", "hdfs://mycluster/address_tsv/")
}

class ZipCodeToAdminCode(args: Args) extends Job(args) {

  override def config: Map[AnyRef, AnyRef] = {
    super.config ++ Map("mapred.job.queue.name" -> "MY_QUEUE",
      "mapred.job.priority" -> args.getOrElse("priority", "NORMAL"))
  }

  val encoding = args.getOrElse("encoding", "utf8")

  val address = TextLine(args("address_input"), textEncoding = encoding).read
    .mapTo('line -> ZipCodeToAdminCode.ADDRESS_SCHEMA) { line: String => val array = line.split("\\|", -1)
    (array(0), array(1), array(2), array(3), array(4), array(5), array(6), array(7), array(8))
  }
    .project('admin_code, 'admin_name, 'zip_code, 'building_name)
    .filter('admin_code) { adminCode: String => !adminCode.isEmpty}
    .filter('zip_code) { zipCode: String => !zipCode.isEmpty}
    .groupBy('zip_code) {
    group => group.sortBy('admin_code)
      .foldLeft('admin_code ->('prev_admin_code, 'admin_code_prefix, 'unique_count, 'admin_code_list))(("", "", 0, "")) {
      (before: (String, String, Int, String), adminCode: String) =>
        val (prevAdminCode, prefix, count, adminCodeList) = before

        if (0 == count) (adminCode, adminCode, 1, adminCode)
        else {
          val newPrefix = (prefix, adminCode).zipped.map { (prefix, code) => if (prefix == code) prefix else '0'}
          val (newCount, newAdminCodeList) =
            if (prevAdminCode == adminCode) (count, adminCodeList) else (count + 1, adminCodeList + ":" + adminCode)
          (adminCode, newPrefix, newCount, newAdminCodeList)
        }
    }
  }
    .project(ZipCodeToAdminCode.CODE_CONVERT_SCHEMA)
    .write(Tsv(args("address_output")))
}