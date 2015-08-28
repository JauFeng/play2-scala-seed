name := """play2-scala-seed"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  ws,
  filters,
  specs2 % Test,

  "com.typesafe.play" %% "play-slick" % "1.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.0.0",
  "com.h2database" % "h2" % "1.4.187",

  "org.webjars" % "jquery" % "2.1.4",
  "org.webjars" % "bootstrap" % "3.3.5",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24",

  "com.typesafe.play" %% "play-mailer" % "3.0.1"
)

resolvers ++= Seq(
  "sorm Scala 2.11 fork" at "http://markusjura.github.io/sorm",
  "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

scalacOptions in Test ++= Seq("-Yrangepos")

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := true

//~ ~~~
//retrieveManaged := true