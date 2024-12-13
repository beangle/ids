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

package org.beangle.ids.cas.service

import org.beangle.cache.redis.RedisCacheManager
import org.beangle.commons.cache.Cache
import org.beangle.commons.event.EventPublisher
import org.beangle.commons.io.DefaultBinarySerializer
import org.beangle.security.session.{OverTryLoginEvent, Session}
import redis.clients.jedis.JedisPool

class LoginRetryServiceImpl extends LoginRetryService, EventPublisher {

  private[this] var failCounts: Cache[String, Int] = _

  //密码错误次数是否3次以上
  var maxAuthTries: Int = 3

  def this(pool: JedisPool) = {
    this()
    val cacheManager = new RedisCacheManager(pool, DefaultBinarySerializer, true)
    cacheManager.ttl = 15 * 60 //15minutes
    failCounts = cacheManager.getCache("login_failcount", classOf[String], classOf[Int])
  }

  override def isOverMaxTries(principal: String): Boolean = {
    getFailCount(principal) >= maxAuthTries
  }

  override def getFailCount(principal: String): Int = {
    failCounts.get(principal).getOrElse(0)
  }

  override def incFailCount(principal: String, agent: Session.Agent): Int = {
    failCounts.get(principal) match
      case None =>
        failCounts.put(principal, 1)
        1
      case Some(fc) =>
        val nfc = fc + 1
        failCounts.put(principal, nfc)
        if (nfc >= maxAuthTries) {
          publish(new OverTryLoginEvent(principal, nfc, agent))
        }
        nfc
  }
}
