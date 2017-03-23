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

import org.beangle.commons.cache.CacheManager
import org.beangle.commons.lang.Strings
import org.beangle.ids.cas.id.ServiceTicketIdGenerator
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.security.authc.{ AuthenticationException, UsernamePasswordToken }
import org.beangle.security.context.SecurityContext
import org.beangle.security.session.Session
import org.beangle.security.web.WebSecurityManager
import org.beangle.security.web.session.CookieSessionIdPolicy
import org.beangle.webmvc.api.action.{ ActionSupport, ServletSupport }
import org.beangle.webmvc.api.annotation.{ ignore, mapping, param }

/**
 * @author chaostone
 */
class LoginAction(secuirtyManager: WebSecurityManager, ticketRegistry: TicketRegistry, sessionServiceCacheManager: CacheManager)
    extends ActionSupport with ServletSupport {

  val sessionServiceCache = sessionServiceCacheManager.getCache("session_service", classOf[String], classOf[java.util.List[String]])

  var serviceTicketIdGenerator: ServiceTicketIdGenerator = _

  @mapping(value = "")
  def index(@param(value = "service", required = false) service: String): Any = {
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

  def success: String = {
    put("logined", SecurityContext.getSession.isDefined)
    forward()
  }

  private def forwardService(service: String, session: Session): Any = {
    if (null == service) {
      redirect("success", null)
    } else {
      if (isMember(service)) {
        redirect(to(service + (if (service.contains("?")) "&" else "?")), null)
      } else {
        val ticket = generateTicket(service, session)
        val sessionId = session.id
        val rs = sessionServiceCache.get(sessionId) match {
          case Some(services) =>
            services.add(service)
            services
          case None =>
            val newServices = new java.util.ArrayList[String]
            newServices.add(service)
            newServices
        }
        sessionServiceCache.put(sessionId, rs)
        redirect(to(service + (if (service.contains("?")) "&" else "?") + "ticket=" + ticket), null)
      }
    }
  }

  private def isMember(service: String): Boolean = {
    val scnameAttr = "scname="
    val sessionCookieNameIndex = service.indexOf(scnameAttr)
    if (-1 == sessionCookieNameIndex) return false
    val scnameChars = service.substring(sessionCookieNameIndex + scnameAttr.length).toCharArray
    var i = 0
    var end = scnameChars.length
    while (i < scnameChars.length) {
      if (!Character.isLetterOrDigit(scnameChars(i))) {
        end = i
        i = scnameChars.length
      }
      i += 1
    }
    val sessionIdPolicy = secuirtyManager.sessionIdPolicy.asInstanceOf[CookieSessionIdPolicy]
    val scname = new String(scnameChars, 0, end)
    if (sessionIdPolicy.name == scname) {
      val serviceDomain = Strings.substringBetween(service, "://", "/")
      val myDomain = if (null == sessionIdPolicy.domain) {
        request.getServerName
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
