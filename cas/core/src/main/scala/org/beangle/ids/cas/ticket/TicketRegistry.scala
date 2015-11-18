package org.beangle.ids.cas.ticket

import org.beangle.security.session.Session

/**
 * @author chaostone
 */
trait TicketRegistry {
  def validateTicket(ticket: String, service: String): Result

  def putTicket(ticket: String, service: String, session: Session)
}