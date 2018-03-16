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
package org.beangle.ids.cas.web.action

import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.security.authc.{ AuthenticationException, UsernamePasswordToken }
import org.beangle.security.context.SecurityContext
import org.beangle.security.session.Session
import org.beangle.security.web.WebSecurityManager
import org.beangle.security.web.session.{ CookieSessionIdPolicy, SessionIdPolicy }
import org.beangle.webmvc.api.action.{ ActionSupport, ServletSupport }
import org.beangle.webmvc.api.annotation.{ mapping, param }
import org.beangle.webmvc.api.context.Params
import org.beangle.webmvc.api.view.View
import org.beangle.ids.cas.web.helper.SessionHelper

/**
 * @author chaostone
 */
class LoginAction(secuirtyManager: WebSecurityManager, ticketRegistry: TicketRegistry)
    extends ActionSupport with ServletSupport {

  @mapping(value = "")
  def index(@param(value = "service", required = false) service: String): View = {
    SecurityContext.getSession match {
      case Some(session) =>
        forwardService(service, session)
      case None =>
        val u = get("username")
        val p = get("password")
        if (u.isEmpty || p.isEmpty) {
          forward()
        } else {
          val token = new UsernamePasswordToken(u.get, p.get)
          try {
            val session = secuirtyManager.login(request, response, token)
            SecurityContext.session = session
            forwardService(service, session)
          } catch {
            case e: AuthenticationException =>
              put("error", "用户名和密码错误")
              forward()
          }
        }
    }
  }

  def success: View = {
    put("logined", SecurityContext.getSession.isDefined)
    forward()
  }

  private def forwardService(service: String, session: Session): View = {
    if (null == service) {
      redirect("success", null)
    } else {
      if (SessionHelper.isMember(request, service, secuirtyManager.sessionIdPolicy)) {
        redirect(to(service), null)
      } else {
        val ticket = ticketRegistry.generate(session, service)
        redirect(to(service + (if (service.contains("?")) "&" else "?") + "ticket=" + ticket), null)
      }
    }
  }
}
