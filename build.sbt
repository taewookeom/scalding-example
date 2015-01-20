name := "scalding-example"

version := "1.0"

scalaVersion := "2.10.4"

val scaldingVersion = "0.12.0"

libraryDependencies ++= Seq(
  // Scala Library
  ("com.twitter" %% "scalding-core" % scaldingVersion).exclude("com.esotericsoftware.minlog", "minlog"),
  "com.github.nscala-time" %% "nscala-time" % "1.4.0",
  // Java Library
  ("org.apache.hadoop" % "hadoop-core" % "2.3.0-mr1-cdh5.0.1").intransitive(),
  ("org.apache.hadoop" % "hadoop-common" % "2.3.0-cdh5.0.1").intransitive(),
  ("org.apache.avro" % "avro" % "1.7.5-cdh5.0.1" % "provided").intransitive(),
  ("commons-logging" % "commons-logging" % "1.2").intransitive(),
  ("commons-collections" % "commons-collections" % "3.2.1").intransitive(),
  ("commons-cli" % "commons-cli" % "1.2").intransitive(),
  ("log4j" % "log4j" % "1.2.17").intransitive()
)

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  val excludes = Set(
    "hadoop-core-2.3.0-mr1-cdh5.0.1.jar",
    "hadoop-common-2.3.0-cdh5.0.1.jar",
    "commons-logging-1.2.jar",
    "commons-collections-3.2.1.jar",
    "commons-cli-1.2.jar",
    "commons-compiler-2.6.1.jar",
    "log4j-1.2.17.jar",
    "slf4j-api-1.6.6.jar",
    "guava-14.0.1.jars",
    "asm-4.0.jar",
    "janino-2.6.1.jar",
    "riffle-0.1-dev.jar",
    "jgrapht-jdk1.6-0.8.1.jar"
  )
  cp filter { jar => excludes(jar.data.getName)}
}

resolvers ++= Seq(
  "Concurrent Maven Repo" at "http://conjars.org/repo",
  "Hadoop Releases" at "https://repository.cloudera.com/content/repositories/releases/"
)

// set the main class for packaging the main jar
// 'run' will still auto-detect and prompt
// change Compile to Test to set it for the test jar
//mainClass in (Compile, packageBin) := Some("myproject.MyMain")
mainClass in(Compile, packageBin) := None

addCommandAlias("make", ";package;assemblyPackageDependency")