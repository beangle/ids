package org.beangle.ids.cas.ticket.impl

import java.security.Principal
import org.beangle.ids.cas.ticket.{ Result, Ticket, TicketRegistry }
import org.beangle.security.session.Session
import org.beangle.ids.cas.ticket.DefaultServiceTicket
import org.beangle.ids.cas.ticket.UserPrincipal
import org.beangle.commons.cache.CacheManager

/**
 * @author chaostone
 */
class CachedTicketRegistry(cacheManager: CacheManager) extends TicketRegistry {
  var cacheName = "cas_tickets"
  val tickets = cacheManager.getCache[String, DefaultServiceTicket](cacheName)

  override def validateTicket(t: String, service: String): Result = {
    if (null == t || null == service) return Result(null, "-1", "ticket and service parameters are required.")
    tickets.get(t) match {
      case Some(ticket) =>
        if (ticket.service != service) Result(null, "2", "service is wrong")
        tickets.evict(t)
        Result(ticket, "", "")
      case None => Result(null, "1", "Cannot find ticket")
    }
  }

  override def putTicket(ticket: String, service: String, session: Session): Unit = {
    val p = new UserPrincipal(session.principal.getName, session.principal.userName)
    tickets.put(ticket, new DefaultServiceTicket(service, p))
  }
}



