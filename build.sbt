import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

ThisBuild / organization := "org.beangle.ids"
ThisBuild / version := "0.3.14"

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

val b_common_core = "org.beangle.commons" %% "beangle-commons-core" % "5.6.8"
val b_data_jdbc = "org.beangle.data" %% "beangle-data-jdbc" % "5.7.11"
val b_cache_redis = "org.beangle.cache" %% "beangle-cache-redis" % "0.1.6"
val b_security_web = "org.beangle.security" %% "beangle-security-web" % "4.3.15"
val b_web_action = "org.beangle.web" %% "beangle-web-action" % "0.4.9"
val b_notify = "org.beangle.notify" %% "beangle-notify-core" % "0.1.3"

val commonDeps = Seq(logback_classic % "test", logback_core % "test", scalatest, b_common_core, b_data_jdbc, b_cache_redis, b_security_web)

lazy val root = (project in file("."))
  .settings()
  .aggregate(cas, sms, web)

lazy val cas = (project in file("cas"))
  .settings(
    name := "beangle-ids-cas",
    common,
    libraryDependencies ++= commonDeps
  )
lazy val sms = (project in file("sms"))
  .settings(
    name := "beangle-ids-sms",
    common,
    libraryDependencies ++= commonDeps,
    libraryDependencies ++= Seq(b_notify)
  )
lazy val web = (project in file("web"))
  .settings(
    name := "beangle-ids-web",
    common,
    libraryDependencies ++= commonDeps,
    libraryDependencies ++= Seq(b_web_action)
  ).dependsOn(cas)

publish / skip := true
