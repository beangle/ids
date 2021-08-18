import Dependencies._
import BuildSettings._
import sbt.url

ThisBuild / organization := "org.beangle.ids"
ThisBuild / version := "0.2.21"

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
ThisBuild / resolvers += Resolver.mavenLocal

lazy val root = (project in file("."))
  .settings()
  .aggregate(cas)

lazy val cas = (project in file("cas"))
  .settings(
    name := "beangle-ids-cas",
    commonSettings,
    libraryDependencies ++= (commonDeps ++ Seq(jcaptcha))
  )

publish / skip := true
