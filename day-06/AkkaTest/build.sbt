name := "AkkaTest"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  // Change this to another test framework if you prefer
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  // Akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.17"
  //"com.typesafe.akka" %% "akka-remote" % "2.3.5",
  //"com.typesafe.akka" %% "akka-testkit" % "2.3.5"
)
