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
import org.beangle.commons.lang.Strings
import org.beangle.commons.net.http.HttpUtils
import org.beangle.web.servlet.util.CookieUtils

import java.security.SecureRandom

class CaptchaHelper(val captchaBaseUrl: String) {

  val imageUrlPattern = s"${captchaBaseUrl}/captcha/image/{id}.jpg"
  val checkUrlPattern = s"${captchaBaseUrl}/captcha/validate/{id}.json?response={response}"
  val verifyUrlPattern = s"${captchaBaseUrl}/captcha/validate/{id}.json?response={response}&destroy=1"

  val secureRandom = new SecureRandom()
  val cookieName = "CAPTCHA_ID"

  /**
   * 60 length random string,with key as salt
   * @return imageurl and checkurl
   */
  def generateCaptchaUrl(request: HttpServletRequest, response: HttpServletResponse): (String, String) = {
    val buffer = new Array[Byte](25)
    secureRandom.nextBytes(buffer)
    val captchaId = Hex.encode(buffer)
    CookieUtils.addCookie(request, response, cookieName, captchaId, -1)
    (Strings.replace(imageUrlPattern, "{id}", captchaId), Strings.replace(checkUrlPattern, "{id}", captchaId))
  }

  def verify(request: HttpServletRequest, response: HttpServletResponse): Boolean = {
    val captchaId = CookieUtils.getCookieValue(request, cookieName)
    CookieUtils.deleteCookieByName(request, response, cookieName)
    if (null == captchaId) {
      false
    } else {
      try {
        var verifyUrl = Strings.replace(verifyUrlPattern, "{id}", captchaId)
        verifyUrl = Strings.replace(verifyUrl, "{response}", request.getParameter("captcha_response"))
        val rs = HttpUtils.get(verifyUrl)
        rs.isOk && rs.getText.contains("success")
      } catch {
        case _: Throwable => false
      }
    }
  }
}
