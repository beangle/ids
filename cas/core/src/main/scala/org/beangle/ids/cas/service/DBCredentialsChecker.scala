package org.beangle.ids.cas.service

import org.beangle.data.jdbc.query.JdbcExecutor
import org.beangle.security.authc.CredentialsChecker
import org.beangle.security.codec.DefaultPasswordEncoder

import javax.sql.DataSource

/**
 * @author chaostone
 */
class DBCredentialsChecker(dataSource: DataSource, passwordSql: String) extends CredentialsChecker {
  private val executor = new JdbcExecutor(dataSource)

  override def check(principal: Any, credential: Any): Boolean = {
    val rs = executor.query(passwordSql, principal)
    if (rs.isEmpty) {
      false
    } else {
      val digest = rs.head.head.asInstanceOf[String]
      DefaultPasswordEncoder.verify(digest, credential.toString)
    }
  }
}
