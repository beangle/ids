/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.ids.cas.service

import org.beangle.security.authc.{CredentialChecker, DBCredentialStore}
import org.beangle.security.codec.DefaultPasswordEncoder
import org.beangle.security.realm.ldap.LdapCredentialStore

class DBLdapCredentialChecker extends CredentialChecker {
  var dbStore: DBCredentialStore = _
  var ldapStore: Option[LdapCredentialStore] = None

  override def check(principal: Any, credential: Any): Boolean = {
    dbStore.getPassword(principal) match {
      case None =>
        ldapStore match {
          case Some(ldap) => validateByLdap(ldap, principal, credential.toString, None)
          case None => false
        }
      case Some(dbpwd) =>
        ldapStore match {
          case Some(ldap) => validateByLdap(ldap, principal, credential.toString, Some(dbpwd))
          case None => DefaultPasswordEncoder.verify(dbpwd, credential.toString)
        }
    }
  }

  def validateByLdap(ldap: LdapCredentialStore, principal: Any, credential: String, dbpass: Option[String]): Boolean = {
    ldap.getActivePassword(principal) match {
      case Some(ps) =>
        if (ps._2) {
          val p = ps._1
          val ldapCorrect = DefaultPasswordEncoder.verify(p, credential)
          if ldapCorrect && p != dbpass.getOrElse(p + ".") then dbStore.updatePassword(principal, p)
          ldapCorrect
        } else {
          println(s"${principal} is in active")
          false
        }
      case None =>
        dbpass match {
          case None => false
          case Some(dbpwd) => DefaultPasswordEncoder.verify(dbpwd, credential)
        }
    }
  }
}
