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

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import jdk.internal.agent.resources.agent
import org.beangle.commons.bean.Initializing
import org.beangle.commons.codec.binary.Aes
import org.beangle.commons.lang.Strings
import org.beangle.ids.cas.CasSetting
import org.beangle.ids.cas.service.{CasService, LoginRetryService, UsernameValidator}
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.ids.cas.web.helper.{CaptchaHelper, CsrfDefender, LoginHelper}
import org.beangle.security.Securities
import org.beangle.security.authc.*
import org.beangle.security.context.SecurityContext
import org.beangle.security.session.Session
import org.beangle.security.web.access.SecurityContextBuilder
import org.beangle.security.web.authc.WebClient
import org.beangle.security.web.{EntryPoint, WebSecurityManager}
import org.beangle.web.servlet.url.UrlBuilder
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.annotation.{ignore, mapping, param}
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.{Status, View}

/**
 * @author chaostone
 */
class LoginAction(securityManager: WebSecurityManager, ticketRegistry: TicketRegistry)
  extends ActionSupport , ServletSupport , Initializing {

  private var csrfDefender: CsrfDefender = _

  var setting: CasSetting = _

  var captchaHelper: CaptchaHelper = _

  var casService: CasService = _

  var entryPoint: EntryPoint = _

  var passwordPolicyProvider: PasswordPolicyProvider = _

  var credentialStore: DBCredentialStore = _

  var securityContextBuilder: SecurityContextBuilder = _

  var loginRetryService: LoginRetryService = _

  override def init(): Unit = {
    csrfDefender = new CsrfDefender(setting.key, setting.origin)
  }

  @mapping(value = "")
  def index(@param(value = "service", required = false) service: String): View = {
    //remote cas single sign out
    get("logoutRequest") match {
      case None =>
      case Some(d) =>
        return if Securities.session.isDefined then redirect(to(classOf[LogoutAction], "index"), null) else null
    }
    Securities.session match {
      case Some(session) =>
        forwardService(service, session)
      case None =>
        val u = get("username")
        val p = get("password")
        if (Strings.isBlank(u.orNull) || Strings.isBlank(p.orNull)) {
          toLoginForm(request, service)
        } else {
          //username and password are provided.
          val isService = getBoolean("isService", defaultValue = false)
          val validCsrf = isService || csrfDefender.valid(request, response)
          val username = u.get.trim()
          if (validCsrf) {
            if (!isService && setting.enableCaptcha && !captchaHelper.verify(request, response)) {
              put("error", "错误的验证码")
              toLoginForm(request, service)
            } else {
              if (UsernameValidator.illegal(username)) {
                put("error", "非法用户名")
                toLoginForm(request, service)
              } else if (loginRetryService.isOverMaxTries(username)) {
                put("error", "密码错误次数过多，请与15分钟再次尝试。")
                toLoginForm(request, service)
              } else {
                var password = p.get
                if (password.startsWith("?")) {
                  password = Aes.ECB.decodeHex(loginKey, password.substring(1))
                }
                val token = new UsernamePasswordToken(username, password)
                try {
                  val req = request
                  if (setting.passwordReadOnly) token.addDetail("credentialReadOnly", true)

                  val session = securityManager.login(req, response, token)
                  SecurityContext.set(securityContextBuilder.build(req, Some(session)))
                  if (isService) {
                    forwardService(service, session)
                  } else {
                    if (setting.passwordReadOnly) {
                      forwardService(service, session)
                    } else {
                      var credentialOk = true
                      var msg = ""
                      if (setting.checkPasswordStrength) {
                        credentialOk = PasswordStrengthChecker.check(password, passwordPolicyProvider.getPolicy)
                        msg = "检测到弱密码，请修改"
                      }
                      if (credentialOk) {
                        credentialStore.getAge(username) foreach { age =>
                          credentialOk = !age.expired
                          msg = s"密码已经过期，请修改"
                        }
                      }
                      if (!credentialOk) {
                        redirect(to("/edit", if (Strings.isNotBlank(service)) "service=" + service else ""), msg)
                      } else {
                        forwardService(service, session)
                      }
                    }
                  }
                } catch {
                  case e: AuthenticationException =>
                    var msg = casService.getMesage(e)
                    if (e.isInstanceOf[BadCredentialException]) {
                      val fc = loginRetryService.incFailCount(username, WebClient.get(request))
                      if (fc < loginRetryService.maxAuthTries) {
                        msg += s",剩余${loginRetryService.maxAuthTries - fc}次机会"
                      } else {
                        msg += ",密码错误次数过多，请与15分钟再次尝试。"
                      }
                    }
                    put("error", msg)
                    toLoginForm(request, service)
                }
              }
            }
          } else {
            null
          }
        }
    }
  }

  @ignore
  def toLoginForm(req: HttpServletRequest, service: String): View = {
    if (entryPoint.isLocalLogin(req, null)) {
      if (casService.isValidClient(service)) {
        if (null != req.getParameter("gateway") && Strings.isNotBlank(service)) {
          redirectService(response, service)
        } else {
          if (setting.forceHttps && !RequestUtils.isHttps(request)) {
            val builder = new UrlBuilder(req.getContextPath)
            builder.setScheme("https").setServerName(req.getServerName).setPort(443)
              .setContextPath(req.getContextPath).setServletPath("/login")
              .setQueryString(req.getQueryString)
            redirectService(response, builder.buildUrl())
          } else {
            csrfDefender.addToken(req, response)
            put("setting", setting)
            if (setting.enableCaptcha) {
              put("captcha_url", captchaHelper.generateCaptchaUrl(request, response))
            }
            put("current_timestamp", System.currentTimeMillis)
            forward("index")
          }
        }
      } else {
        response.getWriter.write("Invalid client")
        Status.Forbidden
      }
    } else {
      entryPoint.remoteLogin(req, response)
      null
    }
  }

  def success(): View = {
    put("logined", Securities.session.isDefined)
    forward()
  }

  private def forwardService(service: String, session: Session): View = {
    new LoginHelper(securityManager, ticketRegistry, casService).forwardService(request, response, this, service, session)
  }

  private def redirectService(response: HttpServletResponse, service: String): View = {
    response.sendRedirect(service)
    null
  }

  /**
   * 用于加密用户密码的公开key，注意不要更改这里16。
   */
  private def loginKey: String = {
    val serverName = request.getServerName
    if (serverName.length >= 16) {
      serverName.substring(0, 16)
    } else {
      Strings.rightPad(serverName, 16, '0')
    }
  }

}
