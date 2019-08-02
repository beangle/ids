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

import org.beangle.commons.lang.Strings
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{action, mapping, param}
import org.beangle.webmvc.api.view.View

/**
 * @author chaostone
 */
@action("serviceValidate")
class ServiceValidateAction(ticketRegistry: TicketRegistry) extends ActionSupport {

  @mapping("")
  def index(@param(value = "service", required = false) service: String, @param(value = "ticket", required = false) ticket: String): View = {
    val result = ticketRegistry.validate(ticket, service)
    put("result", result)
    forward(if (Strings.isEmpty(result.code)) "success" else "failure")
  }
}
