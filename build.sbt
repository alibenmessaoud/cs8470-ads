name := "cs8470-ads"

version := "1.0"

scalaVersion := "2.9.3-RC1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.3"

libraryDependencies += "org.scala-lang" % "scala-swing" % "2.9.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xexperimental")


