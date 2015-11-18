package org.beangle.ids.cas.ticket.impl

import java.security.Principal
import org.beangle.ids.cas.ticket.{ Result, Ticket, TicketRegistry }
import org.beangle.security.session.Session
import org.beangle.ids.cas.ticket.DefaultServiceTicket
import org.beangle.ids.cas.ticket.UserPrincipal

/**
 * @author chaostone
 */
class MemTicketRegistry extends TicketRegistry {
  val tickets = new java.util.concurrent.ConcurrentHashMap[String, DefaultServiceTicket]

  override def validateTicket(t: String, service: String): Result = {
    if (null == t || null == service) return Result(null, "-1", "ticket and service parameters are required.")
    val ticket = tickets.get(t)
    if (null == ticket) return Result(null, "1", "Cannot find ticket")
    if (ticket.service != service) return Result(null, "2", "service is wrong")
    tickets.remove(t)
    Result(ticket, "", "")
  }

  override def putTicket(ticket: String, service: String, session: Session): Unit = {
    val p = new UserPrincipal(session.principal.getName, session.principal.userName)
    tickets.put(ticket, new DefaultServiceTicket(service, p))
  }
}



