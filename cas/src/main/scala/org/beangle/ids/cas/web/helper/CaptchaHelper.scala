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
package org.beangle.ids.cas.web.helper

import com.octo.captcha.service.image.DefaultManageableImageCaptchaService
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.codec.binary.Hex
import org.beangle.commons.web.util.CookieUtils

import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import javax.imageio.ImageIO

class CaptchaHelper {

  var captchaService = new DefaultManageableImageCaptchaService
  captchaService.setCaptchaEngine(new GmailEngine)
  captchaService.setMinGuarantedStorageDelayInSeconds(600)

  val secureRandom = new SecureRandom()
  val cookieName = "CAPTCHA_ID"

  /**
   * 60 length random string,with key as salt
   */
  def generate(request: HttpServletRequest, response: HttpServletResponse): Array[Byte] = {
    val buffer = new Array[Byte](25)
    secureRandom.nextBytes(buffer)
    val captchaId = Hex.encode(buffer)
    CookieUtils.addCookie(request, response, cookieName, captchaId, -1)
    val challenge = captchaService.getImageChallengeForID(captchaId, request.getLocale)
    val os = new ByteArrayOutputStream()
    ImageIO.write(challenge, "JPEG", os)
    os.toByteArray
  }

  def verify(request: HttpServletRequest, response: HttpServletResponse): Boolean = {
    val captchaId = CookieUtils.getCookieValue(request, cookieName)
    CookieUtils.deleteCookieByName(request, response, cookieName)
    if (null == captchaId) {
      false
    } else {
      try {
        captchaService.validateResponseForID(captchaId, request.getParameter("captcha_response"))
      } catch {
        case _: Throwable => false
      }
    }

  }
}
