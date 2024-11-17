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

package org.beangle.ids.sms.service.impl

import org.beangle.commons.cache.Cache
import org.beangle.cache.redis.RedisCacheManager
import org.beangle.commons.io.DefaultBinarySerializer
import org.beangle.ids.sms.service.SmsCacheService
import redis.clients.jedis.JedisPool

class DefaultSmsCacheService(pool: JedisPool) extends SmsCacheService {
  var ttl: Int = 5 * 60
  var verifyCodes = buildCache(pool)

  override def get(mobile: String): Option[String] = {
    verifyCodes.get(mobile)
  }

  override def set(mobile: String, verifyCode: String): Unit = {
    verifyCodes.put(mobile, verifyCode)
  }

  override def remove(mobile: String): Unit = {
    verifyCodes.evict(mobile)
  }

  override def ttlMinutes: Int = ttl / 60

  private def buildCache(pool: JedisPool): Cache[String, String] = {
    val cacheManager = new RedisCacheManager(pool, DefaultBinarySerializer, true)
    cacheManager.ttl = ttl
    cacheManager.getCache("cas_sms", classOf[String], classOf[String])
  }
}
