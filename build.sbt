name := "war-worker"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.0",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.clapper" %% "grizzled-slf4j" % "1.0.2"
)
