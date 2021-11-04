import org.beangle.parent.Dependencies._
import org.beangle.parent.Settings._

ThisBuild / organization := "org.beangle.ids"
ThisBuild / version := "0.2.25"

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

val beangle_data_jdbc = "org.beangle.data" %% "beangle-data-jdbc" % "5.3.26"
val beangle_cache_redis = "org.beangle.cache" %% "beangle-cache-redis" % "0.0.25"
val beangle_security_web = "org.beangle.security" %% "beangle-security-web" % "4.2.32"
val beangle_mvc_freemarker = "org.beangle.webmvc" %% "beangle-webmvc-freemarker" % "0.4.8"

val commonDeps = Seq(logback_classic, logback_core, scalatest,beangle_data_jdbc, beangle_cache_redis,beangle_security_web)

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
    libraryDependencies ++= Seq(beangle_mvc_freemarker)
  ).dependsOn(cas)

publish / skip := true
