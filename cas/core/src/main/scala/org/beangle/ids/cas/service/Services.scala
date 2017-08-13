package org.beangle.ids.cas.service

import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput

class Services extends Externalizable {
  var services: Set[String] = Set.empty

  def add(service: String): Unit = {
    this.services += service
  }

  def writeExternal(out: ObjectOutput) {
    out.writeInt(services.size)
    services.foreach(out.writeObject(_))
  }

  def readExternal(in: ObjectInput) {
    val size = in.readInt()
    ((0 until size) map (in.readObject.asInstanceOf[String])).toList
  }

}