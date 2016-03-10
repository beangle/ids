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

import org.beangle.commons.web.util.CookieUtils
import org.beangle.ids.cas.id.impl.DefaultIdGenerator
import org.beangle.security.web.session.SessionIdPolicy
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.security.web.session.CookieSessionIdPolicy

/**
 * @author chaostone
 */
class DefaultCasSessionIdPolicy(cookieName: String = "TGC") extends CookieSessionIdPolicy(cookieName) {
  private val sessionIdGenerator = new DefaultIdGenerator("TGT-", 35)

  protected def newId(request: HttpServletRequest): String = {
    sessionIdGenerator.nextid()
  }

}