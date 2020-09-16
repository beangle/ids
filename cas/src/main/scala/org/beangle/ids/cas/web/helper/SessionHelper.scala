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
package org.beangle.ids.cas.web.helper

import jakarta.servlet.http.HttpServletRequest
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.util.RequestUtils
import org.beangle.security.web.session.{CookieSessionIdPolicy, SessionIdReader}
import org.beangle.webmvc.api.context.Params

object SessionHelper {

  def isMember(request: HttpServletRequest, service: String, sIdPolicy: CookieSessionIdPolicy): Boolean = {
    Params.get(SessionIdReader.SessionIdName) match {
      case None    => false
      case Some(n) => sIdPolicy.name == n
    }
  }

  def isSameDomain(request: HttpServletRequest, service: String, sessionIdPolicy: CookieSessionIdPolicy): Boolean = {
    if (isSameScheme(request, service)) {
      val startIdx = service.indexOf("://") + 3
      var portIdx = service.indexOf(':', startIdx)
      if (portIdx < 0) portIdx = service.length
      val slashIdx = service.indexOf('/', startIdx)
      val serviceDomain = service.substring(startIdx, Math.min(portIdx, slashIdx))
      val myDomain =
        if (null == sessionIdPolicy.domain) {
          request.getServerName
        } else {
          sessionIdPolicy.domain
        }
      serviceDomain.contains(myDomain)
    } else {
      false
    }
  }

  def isSameScheme(req: HttpServletRequest, service: String): Boolean = {
    val serviceScheme = Strings.substringBefore(service, "://")
    serviceScheme == (if (RequestUtils.isHttps(req)) "https" else "http")
  }
}
