package org.beangle.ids.cas.cache

import org.beangle.cache.CacheManager
import org.beangle.cache.Cache
import org.beangle.ids.cas.ticket.DefaultServiceTicket
import org.beangle.ids.cas.service.Services

class CasCacheService(cacheManager: CacheManager) {

  val tickets = cacheManager.getCache("cas_tickets", classOf[String], classOf[DefaultServiceTicket])

  val services = cacheManager.getCache("cas_services", classOf[String], classOf[Services])

  def getTicketCache(): Cache[String, DefaultServiceTicket] = {
    tickets
  }

  def getServiceCache(): Cache[String, Services] = {
    services
  }
}