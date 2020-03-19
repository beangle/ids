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

import java.io.ByteArrayInputStream

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.bean.Initializing
import org.beangle.commons.codec.binary.Aes
import org.beangle.commons.lang.{Numbers, Strings}
import org.beangle.commons.web.url.UrlBuilder
import org.beangle.commons.web.util.{CookieUtils, RequestUtils}
import org.beangle.ids.cas.CasSetting
import org.beangle.ids.cas.service.{CasService, UsernameValidator}
import org.beangle.ids.cas.ticket.TicketRegistry
import org.beangle.ids.cas.web.helper.{CaptchaHelper, CsrfDefender, SessionHelper}
import org.beangle.security.Securities
import org.beangle.security.authc._
import org.beangle.security.context.SecurityContext
import org.beangle.security.session.Session
import org.beangle.security.web.access.SecurityContextBuilder
import org.beangle.security.web.session.CookieSessionIdPolicy
import org.beangle.security.web.{EntryPoint, WebSecurityManager}
import org.beangle.webmvc.api.action.{ActionSupport, ServletSupport}
import org.beangle.webmvc.api.annotation.{ignore, mapping, param}
import org.beangle.webmvc.api.view.{Status, Stream, View}

/**
 * @author chaostone
 */
class LoginAction(secuirtyManager: WebSecurityManager, ticketRegistry: TicketRegistry)
  extends ActionSupport with ServletSupport with Initializing {

  private var csrfDefender: CsrfDefender = _

  var setting: CasSetting = _

  var captchaHelper: CaptchaHelper = _

  var casService: CasService = _

  var entryPoint: EntryPoint = _

  var passwordPolicyProvider: PasswordPolicyProvider = _

  var credentialStore: DBCredentialStore = _

  var securityContextBuilder: SecurityContextBuilder = _

  override def init(): Unit = {
    csrfDefender = new CsrfDefender(setting.key, setting.origin)
  }

  @mapping(value = "")
  def index(@param(value = "service", required = false) service: String): View = {
    Securities.session match {
      case Some(session) =>
        forwardService(service, session)
      case None =>
        val u = get("username")
        val p = get("password")
        if (u.isEmpty || p.isEmpty) {
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
              } else if (overMaxFailure(username)) {
                put("error", "密码错误三次以上，暂停登录")
                toLoginForm(request, service)
              } else {
                var password = p.get
                if (password.startsWith("?")) {
                  password = Aes.ECB.decodeHex(loginKey, password.substring(1))
                }
                val token = new UsernamePasswordToken(username, password)
                try {
                  val req = request
                  val session = secuirtyManager.login(req, response, token)
                  SecurityContext.set(securityContextBuilder.build(req, Some(session)))
                  if (isService) {
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
                } catch {
                  case e: AuthenticationException =>
                    val msg = casService.getMesage(e)
                    put("error", msg)
                    if (e.isInstanceOf[BadCredentialException]) {
                      rememberFailue(username)
                    }
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

  /** 密码错误次数是否3次以上 */
  def overMaxFailure(princial: String): Boolean = {
    val p = princial.replace("@", "_")
    val c = CookieUtils.getCookieValue(request, "failure_" + p)
    var failure = 0
    if (Strings.isNotBlank(c)) {
      failure = Numbers.toInt(c)
    }
    failure >= 3
  }

  /** 记录密码实效的次数
   * @param princial 账户
   */
  def rememberFailue(princial: String): Unit = {
    val p = princial.replace("@", "_")
    val c = CookieUtils.getCookieValue(request, "failure_" + p)
    var failure = 1
    if (Strings.isNotBlank(c)) {
      failure = Numbers.toInt(c) + 1
    }
    CookieUtils.addCookie(request, response, "failure_" + p, failure.toString, 5 * 60)
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

  def success: View = {
    put("logined", Securities.session.isDefined)
    forward()
  }


  private def forwardService(service: String, session: Session): View = {
    if (null == service) {
      redirect("success", null)
    } else {
      val idPolicy = secuirtyManager.sessionIdPolicy.asInstanceOf[CookieSessionIdPolicy]
      val isMember = SessionHelper.isMember(request, service, idPolicy)
      if (isMember) {
        if (SessionHelper.isSameDomain(request, service, idPolicy)) {
          redirectService(response, service)
        } else {
          if (casService.isValidClient(service)) {
            val serviceWithSid =
              service + (if (service.contains("?")) "&" else "?") + idPolicy.name + "=" + session.id
            redirectService(response, serviceWithSid)
          } else {
            response.getWriter.write("Invalid client")
            Status.Forbidden
          }
        }
      } else {
        if (casService.isValidClient(service)) {
          val ticket = ticketRegistry.generate(session, service)
          redirectService(response, service + (if (service.contains("?")) "&" else "?") + "ticket=" + ticket)
        } else {
          response.getWriter.write("Invalid client")
          Status.Forbidden
        }
      }
    }
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

  def captcha: View = {
    if (setting.enableCaptcha) {
      Stream(new ByteArrayInputStream(captchaHelper.generate(request, response)), "image/jpeg", "captcha")
    } else {
      Status(404)
    }
  }

}
