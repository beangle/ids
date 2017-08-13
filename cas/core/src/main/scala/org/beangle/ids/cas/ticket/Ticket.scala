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
package org.beangle.ids.cas.ticket

import java.io.{ Externalizable, ObjectInput, ObjectOutput }

import org.beangle.security.authc.Account
import org.beangle.security.session.Session
import org.beangle.security.authc.DefaultAccount

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

  def writeExternal(out: ObjectOutput) {
    out.writeObject(sessionId)
    principal.writeExternal(out)
    out.writeObject(service)
  }

  def readExternal(in: ObjectInput) {
    sessionId = in.readObject.asInstanceOf[String]
    principal = new DefaultAccount
    principal.readExternal(in)
    service = in.readObject.asInstanceOf[String]
  }
}