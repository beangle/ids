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

import org.beangle.commons.lang.Strings
import org.beangle.ids.cas.CasSetting
import org.beangle.security.authc._
import org.beangle.security.session.OvermaxSessionException

class CasServiceImpl extends CasService {

  var setting: CasSetting = _
  private val messages: Map[Class[_], String] = Map(
    classOf[AccountExpiredException] -> "账户过期",
    classOf[UsernameNotFoundException] -> "找不到该用户",
    classOf[BadCredentialsException] -> "密码错误",
    classOf[LockedException] -> "账户被锁定",
    classOf[DisabledException] -> "账户被禁用",
    classOf[CredentialsExpiredException] -> "密码过期",
    classOf[OvermaxSessionException] -> "超过最大人数上限"
  )

  override def getMesage(e: Exception): String = {
    messages.getOrElse(e.getClass, e.getMessage)
  }

  override def isValidClient(url: String): Boolean = {
    if (Strings.isEmpty(url)) {
      true
    } else {
      val rs = setting.clients.find { x =>
        url.startsWith(x)
      }
      rs.nonEmpty
    }
  }
}
