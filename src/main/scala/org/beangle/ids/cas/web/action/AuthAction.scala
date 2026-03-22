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

package org.beangle.ids.cas.web.action

import org.beangle.commons.json.JsonObject
import org.beangle.security.Securities
import org.beangle.security.authc.DefaultAccount
import org.beangle.security.web.WebSecurityManager
import org.beangle.web.servlet.url.UrlBuilder
import org.beangle.web.servlet.util.CookieUtils
import org.beangle.webmvc.annotation.param
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.support.ActionSupport
import org.beangle.webmvc.view.View

/** 为了前端调用的登陆和推出服务
 *
 * @param secuirtyManager
 */
class AuthAction(secuirtyManager: WebSecurityManager) extends ActionSupport {

  def login(@param(value = "service", required = false) service: String): View = {
    val rs = new JsonObject()
    Securities.session match {
      case None =>
        val req = ActionContext.current.request
        val builder = UrlBuilder(req)
        builder.setContextPath(req.getContextPath)
        builder.setServletPath("/login")
        if (null != service) {
          builder.setQueryString(UrlBuilder.encodeParams(Map("service" -> service)))
        }
        redirect(to(builder.buildUrl()))
      case Some(session) =>
        rs.add("success", true)
        rs.add("token", session.id)
        val user = new JsonObject()
        val account = session.principal.asInstanceOf[DefaultAccount]
        user.add("code", account.name)
        user.add("name", account.description)
        rs.add("user", user)
        ok(rs)
    }
  }

  def logout(): View = {
    val request = ActionContext.current.request
    val response = ActionContext.current.response
    CookieUtils.deleteCookieByName(request, response, "CAS_service")
    Securities.session foreach { s =>
      secuirtyManager.logout(request, response, s)
    }
    val rs = new JsonObject()
    rs.add("success", true)
    rs.add("message", "Logout Success")
    ok(rs)
  }
}
