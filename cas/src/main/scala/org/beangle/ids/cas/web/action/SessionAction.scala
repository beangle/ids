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
package org.beangle.ids.cas.web.action

import java.time.Instant

import org.beangle.security.web.WebSecurityManager
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{mapping, response}
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.{Status, View}

class SessionAction(secuirtyManager: WebSecurityManager) extends ActionSupport {

  @response
  @mapping("{id}")
  def index(id: String): Any = {
    secuirtyManager.registry.get(id) match {
      case Some(s) => s
      case None    => notfound(id)
    }
  }

  @response
  @mapping("ids/{principal}")
  def ids(principal: String): String = {
    secuirtyManager.registry.findByPrincipal(principal).map(_.id).mkString(",")
  }

  @response
  @mapping("{id}/access")
  def access(id: String): View = {
    val registry = secuirtyManager.registry
    registry.get(id) match {
      case Some(s) =>
        val accessAt = get("time") match {
          case Some(time) => Instant.ofEpochSecond(time.toLong)
          case None       => Instant.now
        }
        registry.access(s.id, accessAt)
        ActionContext.current.response.getWriter.print("ok")
        Status.Ok
      case None => notfound(id)
    }
  }

  private def notfound(id: String): View = {
    ActionContext.current.response.getWriter.print(s"session id $id is not found.");
    Status.NotFound
  }

}
