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
/**
 * @author chaostone
 */
class LogoutAction extends ActionSupport with ServletSupport {
  var casSessionIdPolicy: DefaultCasSessionIdPolicy = _
  @mapping(value = "")
  def index(): View = {
    SecurityContext.getSession match {
      case Some(session) =>
        session.stop()
        val s = request.getSession(false)
        if (null != s) s.invalidate()
        get("service") match {
          case Some(service) => redirect(to(service), null)
          case None          => toLogin()
        }
      case None => toLogin()
    }
  }

  private def toLogin(): View = {
    casSessionIdPolicy.delSessionId(request, response)
    redirect(to(classOf[LoginAction], "index"), null)
  }

}