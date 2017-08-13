package org.beangle.ids.cas.ticket

import org.beangle.cache.Cache
import org.beangle.ids.cas.service.Services

trait TicketCacheService {
  
  def getTicketCache(): Cache[String, DefaultServiceTicket]

  def getServiceCache(): Cache[String, Services]
}