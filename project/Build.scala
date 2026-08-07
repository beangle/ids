import org.beangle.parent.Dependencies.*
import sbt.*

object IdsDepends {
  val b_commons = "org.beangle.commons" % "beangle-commons" % "6.2.1"
  val b_jdbc = "org.beangle.jdbc" % "beangle-jdbc" % "1.1.10"
  val b_cache = "org.beangle.cache" % "beangle-cache" % "0.1.20"
  val b_security = "org.beangle.security" % "beangle-security" % "4.5.0"
  val b_webmvc = "org.beangle.webmvc" % "beangle-webmvc" % "0.15.1"
  val b_notify = "org.beangle.notify" % "beangle-notify" % "0.1.26"

  val coreDepends = Seq(logback_classic % "test", scalatest, b_commons, b_jdbc, b_cache, b_security)
  val casDepends = Seq(b_notify, b_webmvc, jedis)
}
