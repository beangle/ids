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
package org.beangle.ids.cas.ticket

import org.beangle.security.session.Session
import org.beangle.ids.cas.id.ServiceTicketIdGenerator
import org.beangle.ids.cas.service.Services
import org.beangle.commons.event.EventListener
import org.beangle.security.session.LogoutEvent
import org.beangle.commons.event.Event

/**
 * @author chaostone
 */
class DefaultTicketRegistry(cacheService: TicketCacheService)
    extends TicketRegistry with EventListener[LogoutEvent] {

  var serviceTicketIdGenerator: ServiceTicketIdGenerator = _

  private val tickets = cacheService.getTicketCache

  private val services = cacheService.getServiceCache

  override def validate(t: String, service: String): Result = {
    if (null == t || null == service) {
      Result(None, "-1", "ticket and service parameters are required.")
    } else {
      tickets.get(t) match {
        case Some(ticket) =>
          if (ticket.service != service) {
            Result(None, "2", "service is wrong")
          } else {
            tickets.evict(t)
            updateServiceCache(ticket)
            Result(Some(ticket), "", "")
          }
        case None => Result(None, "1", "Cannot find ticket")
      }
    }
  }

  override def generate(session: Session, service: String): String = {
    val ticket = serviceTicketIdGenerator.nextid()
    this.put(session, ticket, service)
    ticket
  }

  override def getServices(session: Session): Option[Services] = {
    services.get(session.id)
  }

  override def evictServices(session: Session): Option[Services] = {
    val rs = services.get(session.id)
    rs foreach { _ => services.evict(session.id) }
    rs
  }

  private def updateServiceCache(st: ServiceTicket): Unit = {
    val rs = services.get(st.sessionId) match {
      case Some(services) =>
        services.add(st.service)
        services
      case None =>
        val newServices = new Services()
        newServices.add(st.service)
        newServices
    }
    services.put(st.sessionId, rs)
  }

  private def put(session: Session, ticket: String, service: String): Unit = {
    tickets.put(ticket, new DefaultServiceTicket(session, service))
  }

  override def onEvent(event: LogoutEvent): Unit = {
    services.evict(event.session.id)
  }

  def supportsEventType(eventType: Class[_ <: Event]): Boolean = {
    classOf[LogoutEvent].isAssignableFrom(eventType)
  }

  override def supportsSourceType(sourceType: Class[_]): Boolean = {
    true
  }
}
