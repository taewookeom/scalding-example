import com.twitter.scalding.Tool
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.util.ToolRunner

object JobRunner {
  def main(args: Array[String]) {
    println(s"Running with arguments: ${args.mkString(" ")}")
    ToolRunner.run(new Configuration, new Tool, args)
  }
}