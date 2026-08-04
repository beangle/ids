import IdsDepends.*
import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

organization := "org.beangle.ids"
version := "0.4.20-SNAPSHOT"

scmInfo := Some(
  ScmInfo(uri("https://github.com/beangle/ids"), "scm:git@github.com:beangle/ids.git")
)

developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = uri("http://github.com/duantihua")
  )
)

description := "The Beangle IDS Library"
homepage := Some(uri("https://beangle.github.io/ids/index.html"))

lazy val root = (project in file("."))
  .settings(
    name := "beangle-ids",
    common,
    libraryDependencies ++= coreDepends,
    libraryDependencies ++= casDepends
  )
