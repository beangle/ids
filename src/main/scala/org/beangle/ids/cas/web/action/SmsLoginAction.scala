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

package org.beangle.ids.cas.web.action

import jakarta.servlet.http.HttpServletResponse
import org.beangle.commons.bean.Initializing
import org.beangle.commons.lang.Strings
import org.beangle.ids.cas.CasSetting
import org.beangle.ids.cas.service.{CasService, LoginRetryService, UserMobileProvider, UsernameValidator}
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.ids.cas.web.helper.{CaptchaHelper, CsrfDefender, LoginHelper}
import org.beangle.ids.sms.service.SmsCodeService
import org.beangle.notify.sms.Receiver
import org.beangle.security.Securities
import org.beangle.security.authc.PreauthToken
import org.beangle.security.context.SecurityContext
import org.beangle.security.session.Session
import org.beangle.security.web.WebSecurityManager
import org.beangle.security.web.access.SecurityContextBuilder
import org.beangle.security.web.authc.WebClient
import org.beangle.webmvc.annotation.{mapping, param}
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.View

class SmsLoginAction(securityManager: WebSecurityManager, ticketRegistry: TicketRegistry)
  extends ActionSupport, ServletSupport, Initializing {

  private var csrfDefender: CsrfDefender = _

  var casService: CasService = _

  var userMobileProvider: UserMobileProvider = _

  var captchaHelper: CaptchaHelper = _

  var smsCodeService: SmsCodeService = _

  var securityContextBuilder: SecurityContextBuilder = _

  var setting: CasSetting = _

  var loginRetryService: LoginRetryService = _

  override def init(): Unit = {
    csrfDefender = new CsrfDefender(setting.key, setting.origin)
  }

  @mapping(value = "")
  def login(@param(value = "service", required = false) service: String): View = {
    Securities.session match {
      case Some(session) => forwardService(service, session)
      case None =>
        val userName = get("username", "--").trim()
        val smsCode = get("smsCode", "--").trim()
        if (userName == "--" || smsCode == "--") {
          toLogin(null)
        } else {
          if (csrfDefender.valid(request, response)) {
            if (setting.enableCaptcha && !captchaHelper.verify(request, response)) {
              toLogin("错误的图片验证码")
            } else {
              if (UsernameValidator.illegal(userName)) {
                toLogin("非法用户名")
              } else if (loginRetryService.isOverMaxTries(userName)) {
                toLogin("密码错误次数过多，暂停登录，请与15分钟再次尝试。")
              } else {
                userMobileProvider.get(userName) match
                  case None => toLogin("该用户未绑定手机。")
                  case Some(mi) =>
                    if (smsCodeService.verify(mi.mobile, smsCode)) {
                      val token = PreauthToken(userName, null)
                      val session = securityManager.login(request, response, token)
                      SecurityContext.set(securityContextBuilder.build(request, Some(session)))
                      forwardService(service, session)
                    } else {
                      val fc = loginRetryService.incFailCount(userName, WebClient.get(request))
                      val msg = if (fc < loginRetryService.maxAuthTries) {
                        s"验证码错误,剩余${loginRetryService.maxAuthTries - fc}次机会"
                      } else {
                        "错误次数过多，暂停登录,请15分钟后再次尝试。"
                      }
                      toLogin(msg)
                    }
              }
            }
          } else {
            null
          }
        }
    }
  }

  def send(): View = {
    val userName = get("username", "")
    val result = userMobileProvider.get(userName) match
      case None => "发送失败，该用户未绑定手机。"
      case Some(mi) =>
        if smsCodeService.validate(mi.mobile) then
          smsCodeService.send(Receiver(mi.mobile, mi.userName))
        else
          s"手机号码${mi.mobile}不正确"
    response.setCharacterEncoding("utf-8")
    response.getWriter.print(result)
    null
  }

  private def forwardService(service: String, session: Session): View = {
    new LoginHelper(securityManager, ticketRegistry, casService).forwardService(request, response, this, service, session)
  }

  def success(): View = {
    forward()
  }

  private def redirectService(response: HttpServletResponse, service: String): View = {
    response.sendRedirect(service)
    null
  }

  private def toLogin(msg: String): View = {
    if (Strings.isNotBlank(msg)) put("error", msg)
    if (setting.enableCaptcha) {
      put("captcha_url", captchaHelper.generateCaptchaUrl(request, response))
    }
    put("current_timestamp", System.currentTimeMillis)
    forward("index")
  }
}
