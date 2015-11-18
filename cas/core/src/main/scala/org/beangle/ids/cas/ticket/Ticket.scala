package org.beangle.ids.cas.ticket

import java.security.Principal

/**
 * @author chaostone
 */
trait Ticket extends Serializable {
  def principal: Principal
}

class DefaultServiceTicket(val service: String, val principal: Principal) extends Ticket

class UserPrincipal(name: String, val userName: String) extends Principal with Serializable {

  override def getName: String = {
    name
  }
}