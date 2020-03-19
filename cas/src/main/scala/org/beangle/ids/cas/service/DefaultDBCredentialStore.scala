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

import java.time.{Instant, LocalDate}

import javax.sql.DataSource
import org.beangle.data.jdbc.query.JdbcExecutor
import org.beangle.security.authc.{CredentialAge, DBCredentialStore, Principals}

class DefaultDBCredentialStore(dataSource: DataSource) extends DBCredentialStore {
  private val executor = new JdbcExecutor(dataSource)

  var passwordSql: String = _

  var updateSql: String = _

  var ageSql: String = _

  override def getPassword(principal: Any): Option[String] = {
    val username = Principals.getName(principal)
    val rs = executor.query(passwordSql, username)
    if (rs.isEmpty) {
      None
    } else {
      Some(rs.head.head.asInstanceOf[String])
    }
  }

  override def updatePassword(principal: Any, newPassword: String): Unit = {
    val username = Principals.getName(principal)
    executor.update(updateSql, username, newPassword)
  }

  override def getAge(principal: Any): Option[CredentialAge] = {
    val username = Principals.getName(principal)
    val rs = executor.query(ageSql, username)
    if (rs.isEmpty) {
      None
    } else {
      val d = rs.head
      Some(CredentialAge(d(0).asInstanceOf[Instant], d(1).asInstanceOf[LocalDate], d(2).asInstanceOf[LocalDate]))
    }
  }
}
