/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.ids.cas.id.impl

import org.beangle.commons.logging.Logging
import org.beangle.ids.cas.id.IdGenerator
import org.beangle.ids.cas.id.RandomStringGenerator
import org.beangle.ids.cas.id.NumericGenerator

/**
 * @author chaostone
 */
class DefaultIdGenerator(numericGenerator: NumericGenerator, randomStringGenerator: RandomStringGenerator, prefix: String)
    extends IdGenerator with Logging {

  private val id = new scala.util.Random(System.currentTimeMillis).nextInt(1000000)

  def this(p: String, maxlength: Int) = {
    this(new DefaultLongNumericGenerator(1), new DefaultRandomStringGenerator(maxlength), p)
  }

  override def nextid(): String = {
    val number = numericGenerator.nextNumber()
    val buffer = new StringBuilder(300)
    buffer.append(prefix).append(number).append('-').append(randomStringGenerator.nextString())
    buffer.append(id)
    buffer.toString
  }
}
