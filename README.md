# scalding-example

# Data Source
* [AddressCodeMapping](http://juso.go.kr/notice/OpenArchivesDetail.do?mgtSn=1592&currentPage=2&searchType=&keyword=&noticeKd=26&type=matching)

# Configuring your environment
* ZipCodeToAdminCode
 * DEFAULT_ARGUMENTS
 * mapred.job.queue.name
* ReadHivePartition
 * DEFAULT_ARGUMENTS
 * mapred.job.queue.name

# Build
* $ sbt package
 * target/scala-2.10/scalding-example_2.10-1.0.jar
* $ sbt assemblyPackageDependency
 * target/scala-2.10/scalding-example-assembly-1.0-deps.jar

# Run examples
## local mode (IntelliJ Run Configuration)
* Main class
 * JobRunner or com.twitter.scalding.Tool
* VM options
 * -Xmx4096m
* Program arguments
 * com.example.scalding.ZipCodeToAdminCode --local--encoding cp949 --address_input address_input.txt --address_output address_output.txt
* Working directory
 * /workspace/scalding-example
* Use class path of module
 * scalding-example

## hdfs mode
### ZipCodeToAdminCode
$ HADOOP_CLASSPATH=/home/taewook/scalding-example/scalding-example-assembly-1.0-deps.jar hadoop jar scalding-example_2.10-1.0.jar JobRunner -libjars /home/taewook/scalding-example/scalding-example-assembly-1.0-deps.jar com.example.scalding.ZipCodeToAdminCode --hdfs --encoding cp949 --priority HIGH --address_input hdfs://mycluster/user/taewook/address/ --address_output hdfs://mycluster/user/taewook/address_result/
### ReadHivePartition
$ HADOOP_CLASSPATH=/home/taewook/scalding-example/scalding-example-assembly-1.0-deps.jar hadoop jar scalding-example_2.10-1.0.jar JobRunner -libjars /home/taewook/scalding-example/scalding-example-assembly-1.0-deps.jar com.example.scalding.ReadHivePartition --hdfs --priority HIGH --reducer 25 --address_output hdfs://mycluster/user/taewook/address_result/ --hive_table hdfs://mycluster/user/taewook/hive_table/ --tsv_output hdfs://mycluster/user/taewook/hive_table_tsv/ --today 20150120
### RunAll
$ HADOOP_CLASSPATH=/home/taewook/scalding-example/scalding-example-assembly-1.0-deps.jar hadoop jar scalding-example_2.10-1.0.jar RunAll -libjars /home/taewook/scalding-example/scalding-example-assembly-1.0-deps.jar
