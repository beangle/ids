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
          if (SessionHelper.isSameDomain(request, service, idPolicy) && isInternalPath(request, service)) {
            // 回跳本站 CAS 内部页面（如扫码确认页登录后回跳）：不签发 ST，直接重定向
            redirectService(response, service)
          } else {
            val ticket = ticketRegistry.generate(session, service)
            redirectService(response, service + (if (service.contains("?")) "&" else "?") + "ticket=" + ticket)
          }
        } else {
          response.getWriter.write("Invalid client")
          Status.Forbidden
        }
      }
    }
  }

  /** 判断 service 是否指向本站 CAS 内部路径（contextPath + /cas/）。
   *
   *  用于登录成功回跳时区分"外部业务应用"与"CAS 自身页面"：
   *  内部页面无需签发 service ticket（如扫码确认页回跳），避免产生无用 ST。
   */
  private def isInternalPath(request: HttpServletRequest, service: String): Boolean = {
    if (service == null) false
    else {
      val path =
        if (service.contains("://")) {
          val startIdx = service.indexOf("://") + 3
          val slashIdx = service.indexOf('/', startIdx)
          if (slashIdx < 0) "" else service.substring(slashIdx)
        } else if (service.startsWith("/")) service
        else ""
      path.startsWith(request.getContextPath + "/cas/")
    }
  }

  private def redirectService(response: HttpServletResponse, service: String): View = {
    response.sendRedirect(service)
    null
  }
}
