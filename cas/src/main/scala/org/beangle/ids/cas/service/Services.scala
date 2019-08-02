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
package org.beangle.ids.cas.service

import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

class Services extends Externalizable {
  var services: Set[String] = Set.empty

  def add(service: String): Unit = {
    this.services += service
  }

  def writeExternal(out: ObjectOutput): Unit = {
    out.writeInt(services.size)
    services.foreach(out.writeObject(_))
  }

  def readExternal(in: ObjectInput): Unit = {
    val size = in.readInt()
    ((0 until size) map (in.readObject.asInstanceOf[String])).toList
  }

}
