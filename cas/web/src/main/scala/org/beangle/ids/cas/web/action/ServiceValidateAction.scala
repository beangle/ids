package org.beangle.ids.cas.web.action

import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.security.context.SecurityContext
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{ action, ignore, mapping, param }
import org.beangle.commons.lang.Strings

/**
 * @author chaostone
 */
@action("serviceValidate")
class ServiceValidateAction(ticketRegistry: TicketRegistry) extends ActionSupport {

  @mapping("")
  def index(@param(value = "service", required = false) service: String, @param(value = "ticket", required = false) ticket: String): String = {
    val result = ticketRegistry.validateTicket(ticket, service)
    put("result", result)
    forward(if (Strings.isEmpty(result.code)) "success" else "failure")
  }
}