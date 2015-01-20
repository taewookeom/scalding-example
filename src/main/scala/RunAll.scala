import com.example.scalding.{ReadHivePartition, ZipCodeToAdminCode}
import com.twitter.scalding.Tool
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.util.ToolRunner

object RunAll {
  def main(args: Array[String]): Unit = {
    println(s"Running: ${args.mkString(" ")}")

    val args1 = (args :+ "com.example.scalding.ZipCodeToAdminCode") ++ ZipCodeToAdminCode.DEFAULT_ARGUMENTS
    println(s"Running: ${args1.mkString(" ")}")
    ToolRunner.run(new Configuration, new Tool, args1)

    val args2 = (args :+ "com.example.scalding.ReadHivePartition") ++ ReadHivePartition.DEFAULT_ARGUMENTS
    println(s"Running: ${args2.mkString(" ")}")
    ToolRunner.run(new Configuration, new Tool, args2)
  }
}