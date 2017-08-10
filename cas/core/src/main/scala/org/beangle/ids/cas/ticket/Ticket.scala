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

import java.security.Principal
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

/**
 * @author chaostone
 */
trait Ticket extends Externalizable {
  def principal: String
}

class DefaultServiceTicket extends Ticket {
  var service: String = _
  var principal: String = _
  def this(service: String, principal: String) {
    this()
    this.service = service
    this.principal = principal
  }

  def writeExternal(out: ObjectOutput) {
    out.writeObject(service)
    out.writeObject(principal)
  }

  def readExternal(in: ObjectInput) {
    service = in.readObject.asInstanceOf[String]
    principal = in.readObject.asInstanceOf[String]
  }
}