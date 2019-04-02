/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
