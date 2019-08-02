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

import java.io.{Externalizable, ObjectInput, ObjectOutput}

import org.beangle.security.authc.DefaultAccount
import org.beangle.security.session.Session

/**
 * @author chaostone
 */
trait Ticket extends Externalizable {
  def sessionId: String
}

trait ServiceTicket extends Ticket {
  def service: String
}

class DefaultServiceTicket extends ServiceTicket {
  var sessionId: String = _
  var principal: DefaultAccount = _
  var service: String = _

  def this(session: Session, service: String) {
    this()
    this.sessionId = session.id
    this.principal = session.principal.asInstanceOf[DefaultAccount]
    this.service = service
  }

  def writeExternal(out: ObjectOutput): Unit = {
    out.writeObject(sessionId)
    principal.writeExternal(out)
    out.writeObject(service)
  }

  def readExternal(in: ObjectInput): Unit = {
    sessionId = in.readObject.asInstanceOf[String]
    principal = new DefaultAccount
    principal.readExternal(in)
    service = in.readObject.asInstanceOf[String]
  }
}
