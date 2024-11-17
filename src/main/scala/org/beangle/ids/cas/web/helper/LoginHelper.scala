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

package org.beangle.ids.cas.web.helper

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.ids.cas.service.CasService
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.security.session.Session
import org.beangle.security.web.WebSecurityManager
import org.beangle.security.web.session.CookieSessionIdPolicy
import org.beangle.webmvc.ToClass
import org.beangle.webmvc.view.{RedirectActionView, Status, View}

class LoginHelper(securityManager: WebSecurityManager, ticketRegistry: TicketRegistry, casService: CasService) {

  def forwardService(request: HttpServletRequest, response: HttpServletResponse, action: Any, service: String, session: Session): View = {
    if (null == service) {
      new RedirectActionView(new ToClass(action.getClass, "success"))
    } else {
      val idPolicy = securityManager.sessionIdPolicy.asInstanceOf[CookieSessionIdPolicy]
      val isMember = SessionHelper.isMember(request, service, idPolicy)
      if (isMember) {
        if (SessionHelper.isSameDomain(request, service, idPolicy)) {
          redirectService(response, service)
        } else {
          if (casService.isValidClient(service)) {
            val serviceWithSid =
              service + (if (service.contains("?")) "&" else "?") + idPolicy.name + "=" + session.id
            redirectService(response, serviceWithSid)
          } else {
            response.getWriter.write("Invalid client")
            Status.Forbidden
          }
        }
      } else {
        if (casService.isValidClient(service)) {
          val ticket = ticketRegistry.generate(session, service)
          redirectService(response, service + (if (service.contains("?")) "&" else "?") + "ticket=" + ticket)
        } else {
          response.getWriter.write("Invalid client")
          Status.Forbidden
        }
      }
    }
  }

  private def redirectService(response: HttpServletResponse, service: String): View = {
    response.sendRedirect(service)
    null
  }
}
