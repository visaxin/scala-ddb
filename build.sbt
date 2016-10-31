name := """logDBManagement"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator


libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)


libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-core" % "2.3.0"

libraryDependencies += "org.reactivemongo" %% "reactivemongo-play-json" % "0.12.0"

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.12.0-play24"






