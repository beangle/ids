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

import javax.sql.DataSource
import org.beangle.data.jdbc.query.JdbcExecutor
import org.beangle.security.authc.CredentialsChecker
import org.beangle.security.codec.DefaultPasswordEncoder
import org.beangle.security.realm.ldap.LdapUserStore

class DBLdapCredentialsChecker(dataSource: DataSource) extends CredentialsChecker {
  private val executor = new JdbcExecutor(dataSource)

  var passwordSql: String = _

  var ldapUserStore: Option[LdapUserStore] = None

  override def check(principal: Any, credential: Any): Boolean = {
    val passwords = executor.query(passwordSql, principal)
    if (passwords.isEmpty) {
      false
    } else {
      val dbpwd = passwords.head.head.asInstanceOf[String]
      ldapUserStore match {
        case None =>
          DefaultPasswordEncoder.verify(dbpwd, credential.toString)
        case Some(store) =>
          val uid = principal.toString
          store.getUserDN(uid) match {
            case Some(dn) =>
              store.getPassword(dn) match {
                case Some(p) =>
                  val ldapCorrect = DefaultPasswordEncoder.verify(p, credential.toString)
                  if (ldapCorrect && p != dbpwd) {
                    executor.update("update usr.users set password=? where code=? ", p, uid)
                  }
                  ldapCorrect
                case None => false
              }
            case None => DefaultPasswordEncoder.verify(dbpwd, credential.toString)
          }
      }
    }
  }


}
