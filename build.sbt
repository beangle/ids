import org.beangle.parent.Dependencies._
import org.beangle.parent.Settings._

ThisBuild / organization := "org.beangle.ids"
ThisBuild / version := "0.2.29-SNAPSHOT"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/ids"),
    "scm:git@github.com:beangle/ids.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "chaostone",
    name  = "Tihua Duan",
    email = "duantihua@gmail.com",
    url   = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle IDS Library"
ThisBuild / homepage := Some(url("https://beangle.github.io/ids/index.html"))

val b_data_jdbc = "org.beangle.data" %% "beangle-data-jdbc" % "5.4.6"
val b_cache_redis = "org.beangle.cache" %% "beangle-cache-redis" % "0.0.27"
val b_security_web = "org.beangle.security" %% "beangle-security-web" % "4.2.35"
val b_web_action = "org.beangle.web" %% "beangle-web-action" % "0.1.0"

val commonDeps = Seq(logback_classic % "test", logback_core % "test", scalatest,b_data_jdbc, b_cache_redis,b_security_web)

lazy val root = (project in file("."))
  .settings()
  .aggregate(cas,web)

lazy val cas = (project in file("cas"))
  .settings(
    name := "beangle-ids-cas",
    common,
    libraryDependencies ++= commonDeps
  )

lazy val web = (project in file("web"))
  .settings(
    name := "beangle-ids-web",
    common,
    libraryDependencies ++= commonDeps ,
    libraryDependencies ++= Seq(b_web_action)
  ).dependsOn(cas)

publish / skip := true
