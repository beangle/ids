package org.beangle.ids.cas.ticket

import org.beangle.cache.CacheManager
import org.beangle.cache.Cache
import org.beangle.ids.cas.service.Services
import org.beangle.ids.cas.service.Services
import org.beangle.commons.io.DefaultBinarySerializer
import org.beangle.cache.redis.RedisCacheManager
import redis.clients.jedis.JedisPool

class DefaultTicketCacheService extends TicketCacheService {

  var tickets: Cache[String, DefaultServiceTicket] = _
  var services: Cache[String, Services] = _

  def this(pool: JedisPool) {
    this()
    val cacheManager = new RedisCacheManager(pool, DefaultBinarySerializer, true)
    cacheManager.ttl = 60
    tickets = cacheManager.getCache("cas_tickets", classOf[String], classOf[DefaultServiceTicket])
    cacheManager.ttl = 6 * 60 * 60 //six hour
    services = cacheManager.getCache("cas_services", classOf[String], classOf[Services])
  }

  override def getTicketCache(): Cache[String, DefaultServiceTicket] = {
    tickets
  }

  override def getServiceCache(): Cache[String, Services] = {
    services
  }
}