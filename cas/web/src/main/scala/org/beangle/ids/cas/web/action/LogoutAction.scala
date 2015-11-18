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