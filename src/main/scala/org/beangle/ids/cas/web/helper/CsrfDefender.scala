/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.ids.cas.web.helper

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.codec.binary.Hex
import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging
import org.beangle.commons.net.Networks
import org.beangle.web.servlet.util.CookieUtils

import java.net.URL
import java.security.SecureRandom

/**
 *
 * @see "https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)_Prevention_Cheat_Sheet"
 * @see "https://wiki.mozilla.org/Security/Origin"
 * @see "https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie"
 * @see "https://chloe.re/2016/04/13/goodbye-csrf-samesite-to-the-rescue/"
 */
class CsrfDefender(key: String, target: URL) extends Logging {

  var tokenName = "CSRF_TOKEN"

  val secureRandom = new SecureRandom()

  def this(key: String, origin: String) = {
    this(key, Networks.url(origin))
  }

  def validSource(req: HttpServletRequest): (Boolean, String) = {
    var source = req.getHeader("Origin")
    if (Strings.isBlank(source)) {
      source = req.getHeader("Referer")
      if (Strings.isBlank(source)) {
        return (false, "Empty Origin or Referer header")
      }
    }

    //Compare the source against the expected target origin
    try {
      val sourceURL = Networks.url(source)
      if (!target.getProtocol.equals(sourceURL.getProtocol)
        || !target.getHost.equals(sourceURL.getHost)
        || target.getPort != sourceURL.getPort) {
        (false, s"${source} doesnot match ${target}")
      } else {
        (true, "")
      }
    } catch {
      case e: Throwable =>
        logger.error(s"error source:$source for ${e.getMessage}")
        (false, e.getMessage)
    }
  }

  def valid(req: HttpServletRequest, res: HttpServletResponse): Boolean = {
    if (!req.getMethod.equalsIgnoreCase("POST")) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Only Support Http post method.")
      return false
    }
    val rs = validSource(req)
    if (rs._1) {
      val token = CookieUtils.getCookieValue(req, tokenName)
      if (Strings.isEmpty(token) || !valid(token)) {
        res.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRFToken is absent or invalid.")
        false
      } else {
        CookieUtils.deleteCookieByName(req, res, tokenName)
        true
      }
    } else {
      res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid source.")
      false
    }
  }

  def addToken(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    CookieUtils.addCookie(req, res, tokenName, generateToken(), -1)
  }

  /**
   * 60 length random string,with key as salt
   */
  def generateToken(): String = {
    val buffer = new Array[Byte](25)
    secureRandom.nextBytes(buffer)
    val token = Hex.encode(buffer)
    val hash = Digests.md5Hex(token + key)
    token + hash.substring(0, 10)
  }

  private def valid(token: String): Boolean = {
    if (token.length != 60) {
      false
    } else {
      val t = token.substring(0, 50)
      Digests.md5Hex(t + key).substring(0, 10) == token.substring(50)
    }
  }
}
