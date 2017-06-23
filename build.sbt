organization := "com.nitendragautam"

name := "KafkaStreamingConsumerJob"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
mainClass in assembly := Some("ccom.nitendragautam.consumerjob.main.Boot")
assemblyJarName in assembly := "consumerjob.jar"
libraryDependencies ++= {
  val akkaV       = "2.4.11"
  val akkaHttp ="10.0.6"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" % "akka-stream_2.11" % akkaV,
    "com.typesafe.akka" % "akka-http-spray-json_2.11" % akkaHttp ,
    "net.cakesolutions" %% "scala-kafka-client" % "0.10.1.1",
    "net.cakesolutions" %% "scala-kafka-client-akka" % "0.10.1.1",
    "commons-io" % "commons-io" % "2.5",
    "org.slf4j" % "slf4j-log4j12" % "1.7.21",
    "log4j" % "log4j" % "1.2.17",
    "org.influxdb" % "influxdb-java" % "2.6",
    "com.google.code.gson" % "gson" % "2.8.1"
  )
}



