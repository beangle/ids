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