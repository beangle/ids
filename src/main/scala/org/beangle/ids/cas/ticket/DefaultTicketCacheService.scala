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

package org.beangle.ids.cas.ticket

import org.beangle.commons.cache.Cache
import org.beangle.cache.redis.RedisCacheManager
import org.beangle.commons.io.DefaultBinarySerializer
import org.beangle.ids.cas.service.Services
import redis.clients.jedis.JedisPool

class DefaultTicketCacheService extends TicketCacheService {

  private[this] var tickets: Cache[String, DefaultServiceTicket] = _
  private[this] var services: Cache[String, Services] = _

  def this(pool: JedisPool) = {
    this()
    DefaultBinarySerializer.registerClass(classOf[Services])
    DefaultBinarySerializer.registerClass(classOf[DefaultServiceTicket])

    val cacheManager = new RedisCacheManager(pool, DefaultBinarySerializer, true)
    cacheManager.ttl = 60
    tickets = cacheManager.getCache("cas_tickets", classOf[String], classOf[DefaultServiceTicket])
    cacheManager.ttl = 6 * 60 * 60 //six hour
    services = cacheManager.getCache("cas_services", classOf[String], classOf[Services])
  }

  override def getTicketCache: Cache[String, DefaultServiceTicket] = {
    tickets
  }

  override def getServiceCache: Cache[String, Services] = {
    services
  }
}
