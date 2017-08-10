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
package org.beangle.ids.cas.ticket.impl

import org.beangle.cache.CacheManager
import org.beangle.ids.cas.ticket.{ DefaultServiceTicket, Result, TicketRegistry }
import org.beangle.security.session.Session

/**
 * @author chaostone
 */
class CachedTicketRegistry(cacheManager: CacheManager) extends TicketRegistry {
  var cacheName = "cas_tickets"
  val tickets = cacheManager.getCache(cacheName, classOf[String], classOf[DefaultServiceTicket])

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
    tickets.put(ticket, new DefaultServiceTicket(service, session.principal.getName))
  }
}
