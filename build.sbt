name := """amazon-scrap"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.3-M1",
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.webjars" % "requirejs" % "2.1.11-1",
  "org.webjars" % "underscorejs" % "1.6.0-3",
  "org.jsoup" % "jsoup" % "1.7.3"
)

lazy val root = (project in file(".")).addPlugins(PlayScala)
