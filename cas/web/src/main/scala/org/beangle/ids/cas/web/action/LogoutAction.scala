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

import org.beangle.security.context.SecurityContext
import org.beangle.webmvc.api.action.{ ActionSupport, ServletSupport }
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.view.View
import org.beangle.cache.CacheManager
import org.beangle.security.web.WebSecurityManager
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.security.session.Session
import org.beangle.ids.cas.service.Services

/**
 * @author chaostone
 */
class LogoutAction(secuirtyManager: WebSecurityManager, ticketRegistry: TicketRegistry)
    extends ActionSupport with ServletSupport {

  @mapping(value = "")
  def index(): View = {
    SecurityContext.getSession match {
      case Some(session) =>
        ticketRegistry.evictServices(session) match {
          case Some(services) =>
            put("services", services.services)
            forward("service")
          case None =>
            secuirtyManager.logout(request, response, session)
            get("service") match {
              case Some(service) => redirect(to(service), null)
              case None => toLogin()
            }
        }
      case None =>
        get("service") match {
          case Some(service) => redirect(to(service), null)
          case None          => toLogin()
        }
    }
  }

  private def toLogin(): View = {
    redirect(to(classOf[LoginAction], "index"), null)
  }

}
