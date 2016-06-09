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
package org.beangle.ids.cas.web.action

import org.beangle.ids.cas.web.helper.DefaultCasSessionIdPolicy
import org.beangle.security.context.SecurityContext
import org.beangle.webmvc.api.action.{ ActionSupport, ServletSupport }
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.view.View
import org.beangle.commons.cache.CacheManager

/**
 * @author chaostone
 */
class LogoutAction(sessionServiceCacheManager: CacheManager) extends ActionSupport with ServletSupport {
  var casSessionIdPolicy: DefaultCasSessionIdPolicy = _

  val sessionServiceCache = sessionServiceCacheManager.getCache("session_service", classOf[String], classOf[java.util.List[String]])

  @mapping(value = "")
  def index(): View = {
    SecurityContext.getSession match {
      case Some(session) =>
        val services = sessionServiceCache.get(session.id)
        if (!services.isEmpty) {
          get("service") match {
            case Some(s) => redirect(to(classOf[LogoutAction], "service", "&service=" + s), null)
            case None    => redirect(to(classOf[LogoutAction], "service"), null)
          }
        } else {
          session.stop()
          val s = request.getSession(false)
          if (null != s) s.invalidate()
          get("service") match {
            case Some(service) => redirect(to(service), null)
            case None          => toLogin()
          }
        }
      case None => toLogin()
    }
  }

  def service(): String = {
    put("services", new java.util.ArrayList[String])
    SecurityContext.getSession match {
      case Some(session) =>
        sessionServiceCache.get(session.id) foreach (services => put("services", services))
        sessionServiceCache.evict(session.id)
        session.stop()
      case None =>
    }
    forward()
  }

  private def toLogin(): View = {
    casSessionIdPolicy.delSessionId(request, response)
    redirect(to(classOf[LoginAction], "index"), null)
  }

}
