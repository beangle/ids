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
package org.beangle.ids.cas.id.impl

import org.beangle.ids.cas.id.NumericGenerator
import java.util.concurrent.atomic.AtomicLong

/**
 * @author chaostone
 */
class DefaultLongNumericGenerator(count: AtomicLong) extends NumericGenerator {

  def this(initValue: Long) {
    this(new AtomicLong(initValue))
  }

  override def nextNumber: String = {
    val nextValue =
      if (this.count.compareAndSet(java.lang.Long.MAX_VALUE, 0)) java.lang.Long.MAX_VALUE
      else this.count.getAndIncrement()
    java.lang.Long.toString(nextValue)
  }

}
