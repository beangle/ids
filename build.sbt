import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*
import sbt.Keys.libraryDependencies

ThisBuild / organization := "org.beangle.ids"
ThisBuild / version := "0.3.17"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/ids"),
    "scm:git@github.com:beangle/ids.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle IDS Library"
ThisBuild / homepage := Some(url("https://beangle.github.io/ids/index.html"))

val b_common = "org.beangle.commons" % "beangle-commons" % "5.6.16"
val b_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % "1.0.1"
val b_cache = "org.beangle.cache" % "beangle-cache" % "0.1.9"
val b_security = "org.beangle.security" % "beangle-security" % "4.3.20"
val b_web = "org.beangle.web" % "beangle-web" % "0.4.12"
val b_notify = "org.beangle.notify" % "beangle-notify" % "0.1.6"

lazy val root = (project in file("."))
  .settings(
    name := "beangle-ids",
    common,
    libraryDependencies ++=  Seq(logback_classic % "test", scalatest, b_common, b_jdbc, b_cache, b_security),
    libraryDependencies ++= Seq(b_notify, b_web, jedis)
  )
