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
package org.beangle.ids.cas.web.helper

import org.beangle.security.web.session.{ SessionIdPolicy, SessionIdReader }
import org.beangle.security.web.session.CookieSessionIdPolicy
import org.beangle.webmvc.api.context.Params
import javax.servlet.http.HttpServletRequest

object SessionHelper {

  def isMember(request: HttpServletRequest, service: String, sIdPolicy: SessionIdPolicy): Boolean = {
    val sidName = Params.get(SessionIdReader.SessionIdName)
    if (sidName.isEmpty) return false
    val sessionIdPolicy = sIdPolicy.asInstanceOf[CookieSessionIdPolicy]
    if (sessionIdPolicy.name == sidName.get) {
      val startIdx = service.indexOf("://") + 3
      var portIdx = service.indexOf(':', startIdx)
      if (portIdx < 0) portIdx = service.length
      val slashIdx = service.indexOf('/', startIdx)
      val serviceDomain = service.substring(startIdx, Math.min(portIdx, slashIdx))
      val requestDomain = request.getServerName
      val myDomain = if (null == sessionIdPolicy.domain) {
        requestDomain
      } else {
        if (request.getServerName.contains(sessionIdPolicy.domain)) {
          sessionIdPolicy.domain
        } else {
          request.getServerName
        }
      }
      serviceDomain.contains(myDomain)
    } else {
      false
    }
  }

}
