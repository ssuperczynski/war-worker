name := "war-worker"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "net.logstash.logback" % "logstash-logback-encoder" % "3.0"
)
