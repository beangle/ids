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

import jakarta.servlet.http.HttpServletRequest
import org.beangle.commons.web.util.CookieUtils
import org.beangle.ids.cas.CasSetting
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.security.Securities
import org.beangle.security.authc.DefaultAccount
import org.beangle.security.web.WebSecurityManager
import org.beangle.webmvc.api.action.{ActionSupport, ServletSupport}
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.view.View

/**
 * @author chaostone
 */
class LogoutAction(secuirtyManager: WebSecurityManager, ticketRegistry: TicketRegistry)
  extends ActionSupport with ServletSupport {

  var casSetting: CasSetting = _

  @mapping(value = "")
  def index(): View = {
    Securities.session match {
      case Some(session) =>
        val isRemote = session.principal.asInstanceOf[DefaultAccount].isRemote
        ticketRegistry.evictServices(session) match {
          case Some(services) =>
            put("services", services.services)
            forward("service")
          case None =>
            secuirtyManager.logout(request, response, session)
            //remove remote cas redirect hint(if possiable).
            CookieUtils.deleteCookieByName(request,response,"CAS_service")
            get("service") match {
              case Some(service) => redirect(to(service), null)
              case None => toLogin(request, isRemote)
            }
        }
      case None =>
        get("service") match {
          case Some(service) => redirect(to(service), null)
          case None => toLogin(request, false)
        }
    }
  }

  private def toLogin(request: HttpServletRequest, isRemote: Boolean): View = {
    if (isRemote && casSetting.remoteLogoutUrl.isDefined) {
      redirect(to(casSetting.remoteLogoutUrl.get), null)
    } else {
      redirect(to(classOf[LoginAction], "index"), null)
    }
  }

}
