import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*
import sbt.Keys.libraryDependencies

ThisBuild / organization := "org.beangle.ids"
ThisBuild / version := "0.3.16"

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

val b_common = "org.beangle.commons" % "beangle-commons" % "5.6.15"
val b_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % "1.0.0"
val b_cache = "org.beangle.cache" % "beangle-cache" % "0.1.8"
val b_security = "org.beangle.security" % "beangle-security" % "4.3.19"
val b_web = "org.beangle.web" % "beangle-web" % "0.4.11"
val b_notify = "org.beangle.notify" % "beangle-notify" % "0.1.5"

val commonDeps = Seq(logback_classic % "test", logback_core % "test", scalatest, b_common, b_jdbc, b_cache, b_security)

lazy val root = (project in file("."))
  .settings(
    name := "beangle-ids-cas",
    common,
    libraryDependencies ++= commonDeps,
    libraryDependencies ++= Seq(b_notify, b_web, jedis)
  )
