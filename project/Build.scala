import sbt.Keys._
import sbt._

object BuildSettings {
  val buildScalaVersion = "3.0.1"

  val commonSettings = Seq(
    organizationName := "The Beangle Software",
    licenses += ("GNU Lesser General Public License version 3", new URL("http://www.gnu.org/licenses/lgpl-3.0.txt")),
    startYear := Some(2005),
    scalaVersion := buildScalaVersion,
    scalacOptions := Seq("-Xtarget:11", "-deprecation", "-feature"),
    crossPaths := true,

    publishMavenStyle := true,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishM2Configuration := publishM2Configuration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),

    versionScheme := Some("early-semver"),
    pomIncludeRepository := { _ => false }, // Remove all additional repository other than Maven Central from POM
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    })
}

object Dependencies {
  val logbackVer = "1.2.4"
  val scalatestVer = "3.2.9"
  val scalaxmlVer = "2.0.1"
  val commonsVer = "5.2.5"
  val dataVer = "5.3.24"
  val webVer = "0.0.1"
  val cacheVer= "0.0.23"
  val securityVer= "4.2.30"
  val mvcVer="0.4.4"

  val jcaptchaVer="1.0"

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVer % "test"
  val scalaxml = "org.scala-lang.modules" %% "scala-xml" % scalaxmlVer
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"

  val commonsCore = "org.beangle.commons" %% "beangle-commons-core" % commonsVer
  val dataJdbc = "org.beangle.data" %% "beangle-data-jdbc" % dataVer
  val cacheRedis = "org.beangle.cache" %% "beangle-cache-redis" % cacheVer
  val securityWeb = "org.beangle.security" %% "beangle-security-web" % securityVer
  val mvcFreemarker = "org.beangle.webmvc" %% "beangle-webmvc-freemarker" % mvcVer

  val jcaptcha ="com.octo.captcha" % "jcaptcha" % jcaptchaVer exclude("javax.servlet", "servlet-api")

  var commonDeps = Seq(commonsCore,  logbackClassic, logbackCore, scalatest,dataJdbc, cacheRedis,securityWeb,mvcFreemarker )
}

