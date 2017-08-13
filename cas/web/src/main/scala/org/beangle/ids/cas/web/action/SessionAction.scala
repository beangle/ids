package org.beangle.ids.cas.web.action

import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.annotation.response
import org.beangle.security.session.Session
import org.beangle.security.web.WebSecurityManager
import org.beangle.webmvc.api.view.Status
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.api.context.ActionContext
import java.time.Instant

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
    ActionContext.current.response.getWriter.print(s"session id ${id} is not found.");
    Status.NotFound
  }

}