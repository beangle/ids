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
package org.beangle.ids.cas.id.impl

import org.beangle.ids.cas.id.RandomStringGenerator
import java.security.SecureRandom

object DefaultRandomStringGenerator {
  val Printables = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345679".toCharArray

  def toString(bytes: Array[Byte]): String = {
    val maxlength = bytes.length
    val printableLength = Printables.length
    val output = Array.ofDim[Char](maxlength)
    var i = 0
    while (i < maxlength) {
      val index = Math.abs(bytes(i) % printableLength)
      output(i) = Printables(index)
      i += 1
    }
    new String(output)
  }
}

/**
 * @author chaostone
 */
class DefaultRandomStringGenerator(maxlength: Int) extends RandomStringGenerator {

  private val randomizer = new SecureRandom

  override def nextString: String = {
    val random = Array.ofDim[Byte](maxlength)
    this.randomizer.nextBytes(random)
    DefaultRandomStringGenerator.toString(random)
  }
}