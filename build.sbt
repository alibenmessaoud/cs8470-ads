name := "cs8470-ads"

version := "1.0"

scalaVersion := "2.9.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.3"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xexperimental")

