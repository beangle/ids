/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.ids.cas.web.action

import org.beangle.cache.CacheManager
import org.beangle.ids.cas.id.ServiceTicketIdGenerator
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
import org.beangle.ids.cas.cache.CasCacheService
import org.beangle.ids.cas.service.Services

/**
 * @author chaostone
 */
class LoginAction(secuirtyManager: WebSecurityManager, ticketRegistry: TicketRegistry, casCacheService: CasCacheService)
    extends ActionSupport with ServletSupport {

  val serviceCache = casCacheService.getServiceCache()

  var serviceTicketIdGenerator: ServiceTicketIdGenerator = _

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
      if (isMember(service)) {
        redirect(to(service), null)
      } else {
        val ticket = generateTicket(service, session)
        val sessionId = session.id
        val rs = serviceCache.get(sessionId) match {
          case Some(services) =>
            services.add(service)
            services
          case None =>
            val newServices = new Services()
            newServices.add(service)
            newServices
        }
        serviceCache.put(sessionId, rs)
        redirect(to(service + (if (service.contains("?")) "&" else "?") + "ticket=" + ticket), null)
      }
    }
  }

  private def isMember(service: String): Boolean = {
    val sidName = Params.get(SessionIdPolicy.SessionIdName)
    if (sidName.isEmpty) return false
    val sessionIdPolicy = secuirtyManager.sessionIdPolicy.asInstanceOf[CookieSessionIdPolicy]
    if (sessionIdPolicy.name == sidName.get) {
      val startIdx = service.indexOf("://") + 3
      var portIdx = service.indexOf(':', startIdx)
      if (portIdx < 0) portIdx = service.length
      val slashIdx = service.indexOf('/', startIdx)
      val serviceDomain = service.substring(startIdx, Math.min(portIdx, slashIdx))
      val requestDomain = request.getServerName
      val myDomain = if (null == sessionIdPolicy.domain) {
        requestDomain
      } else {
        if (request.getServerName.contains(sessionIdPolicy.domain)) {
          sessionIdPolicy.domain
        } else {
          request.getServerName
        }
      }
      serviceDomain.contains(myDomain)
    } else {
      false
    }
  }

  private def generateTicket(service: String, session: Session): String = {
    val id = serviceTicketIdGenerator.nextid()
    ticketRegistry.putTicket(id, service, session)
    id
  }

}
