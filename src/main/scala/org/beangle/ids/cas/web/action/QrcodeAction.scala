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
import org.beangle.commons.bean.Initializing
import org.beangle.commons.json.JsonObject
import org.beangle.commons.lang.Strings
import org.beangle.ids.cas.CasSetting
import org.beangle.ids.cas.id.impl.DefaultRandomStringGenerator
import org.beangle.ids.cas.service.{CasAppInfoProvider, CasService, QrcodeService}
import org.beangle.ids.cas.ticket.{QrcodeRecord, TicketRegistry}
import org.beangle.ids.cas.web.helper.{CsrfDefender, LoginHelper}
import org.beangle.security.Securities
import org.beangle.security.authc.PreauthToken
import org.beangle.security.context.SecurityContext
import org.beangle.security.web.WebSecurityManager
import org.beangle.security.web.access.SecurityContextBuilder
import org.beangle.web.servlet.url.UrlBuilder
import org.beangle.web.servlet.sse.{SseEvent, SseWriter}
import org.beangle.web.servlet.util.RequestUtils
import org.beangle.webmvc.annotation.{mapping, param, response}
import org.beangle.webmvc.support.{ActionSupport, ServletSupport}
import org.beangle.webmvc.view.View

import java.io.IOException

/** 扫码登录。
 *
 * 已登录的手机端扫描设备二维码并确认授权，设备凭一次性授权码访问 CAS 建立自己的会话。
 * 二维码渲染由应用客户端负责，本 action 仅提供认证接入接口。
 */
class QrcodeAction(securityManager: WebSecurityManager, ticketRegistry: TicketRegistry)
  extends ActionSupport, ServletSupport, Initializing {

  /** CAS 全局设置（key/origin 用于 CSRF，clients 用于 service 白名单） */
  var setting: CasSetting = _
  /** 校验 service 是否在白名单内 */
  var casService: CasService = _
  /** 二维码记录存取服务（基于 Redis 缓存） */
  var qrcodeService: QrcodeService = _
  /** 根据 service 解析应用展示信息，解析不到视为非法应用 */
  var appInfoProvider: CasAppInfoProvider = _
  /** 设备登录时构建设备自身的安全上下文 */
  var securityContextBuilder: SecurityContextBuilder = _

  /** CSRF 防御器，confirm/cancel 等写操作必须通过校验 */
  private var csrfDefender: CsrfDefender = _

  override def init(): Unit = {
    csrfDefender = new CsrfDefender(setting.key, setting.origin)
  }

  /** POST 创建二维码票据。
   *
   * 设备发起登录：校验 service 合法后生成二维码标识与轮询密钥，
   * 记录存缓存并返回二维码渲染所需信息。
   *
   * @param service      设备登录成功后的跳转地址
   * @param name         应用名称，用于确认页识别应用
   * @param lastQrcodeId 上次二维码标识，可空；传入时创建新码前先作废旧码（refresh）
   * @return { qrcodeId, secret, expireAt, scanUrl }
   */
  @mapping(value = "create", methods = "post")
  @response
  def create(@param("service") service: String,
             @param("name") appName: String,
             @param(value = "lastQrcodeId", required = false) lastQrcodeId: String): JsonObject = {
    val rs = new JsonObject()
    addCorsHeaders(request, response)
    // service 缺失或不在白名单内时直接拒绝创建
    if service == null || service.isEmpty || !casService.isValidClient(service) then {
      rs.add("error", "invalid_service")
      return error(400, rs)
    }
    // 刷新场景：上次的码作废，避免旧码继续被扫描/轮询
    if lastQrcodeId != null && lastQrcodeId.nonEmpty then qrcodeService.evict(lastQrcodeId)
    // qrcodeId 用于标识二维码，secret 仅创建设备知晓，供轮询时鉴权
    val qrcodeId = new DefaultRandomStringGenerator(32).nextString()
    val secret = new DefaultRandomStringGenerator(48).nextString()
    val record = qrcodeService.create(qrcodeId, secret, service, appName, RequestUtils.getIpAddr(request), setting.qrcodeExpireSeconds)
    rs.add("qrcodeId", qrcodeId)
    rs.add("secret", secret)
    rs.add("expireAt", record.expireAt)
    rs.add("scanUrl", buildScanUrl(request, qrcodeId))
    rs
  }

  /** GET 查询二维码状态（设备轮询，需携带 secret）。
   *
   * 设备以固定间隔轮询本接口；secret 不匹配或记录已过期均返回 expired，
   * 设备据此停止轮询并提示失败。
   *
   * @param qrcodeId 二维码标识
   * @param secret   创建时返回的轮询密钥
   * @return { status: pending|scanned|confirmed|cancelled|expired, authToken? }
   */
  @response
  def status(@param("qrcodeId") qrcodeId: String,
             @param("secret") secret: String): JsonObject = {
    val rs = new JsonObject()
    addCorsHeaders(request, response)
    qrcodeService.get(qrcodeId) match {
      case None => rs.add("status", "expired")
      case Some(record) =>
        // secret 作为轮询鉴权凭证，错误时同样按过期处理，避免泄露登录进度
        if secret == null || secret.isEmpty || record.secret != secret then {
          rs.add("status", "expired")
        } else {
          rs.add("status", record.status)
          // 仅确认后才下发一次性授权码，供设备换取会话
          if record.status == QrcodeRecord.Confirmed then rs.add("authToken", record.authToken)
        }
    }
    rs
  }

  /** GET 设备订阅扫码状态（SSE）。
   *
   * 设备通过 EventSource 长连接实时接收状态推送，无需轮询 status。
   * 服务端同步阻塞当前工作线程，定时查询记录，状态变化即推送命名事件；
   * 到达终态（confirmed/cancelled/expired）或客户端断开后关闭连接。
   * 空闲时每 15 秒发送一次 ping 心跳，防止代理/容器断开空闲连接。
   *
   * 事件名与 data JSON：
   *  - `pending`  { "status": "pending" }
   *  - `scanned`  { "status": "scanned" }
   *  - `confirmed` { "status": "confirmed", "authToken": "..." }
   *  - `cancelled` { "status": "cancelled" }
   *  - `expired`  { "status": "expired" }
   *
   * @param qrcodeId 二维码标识
   * @param secret   创建时返回的轮询密钥
   */
  @response
  def stream(@param("qrcodeId") qrcodeId: String,
             @param("secret") secret: String): String = {
    addCorsHeaders(request, response)
    val sse = new SseWriter(response)
    sse.start()
    val expiredEvent = SseEvent.name("expired").data("""{"status":"expired"}""")
    // 记录不存在或 secret 不匹配：直接推送 expired 并关闭，避免泄露登录进度
    qrcodeService.get(qrcodeId) match {
      case None =>
        sse.send(expiredEvent)
        sse.complete()
      case Some(record) if secret == null || secret.isEmpty || record.secret != secret =>
        sse.send(expiredEvent)
        sse.complete()
      case Some(_) =>
        var lastStatus: String = null
        var closed = false
        var idleSeconds = 0
        sse.onError(_ => closed = true)
        try {
          while !closed do
            qrcodeService.get(qrcodeId) match {
              case None =>
                sse.send(expiredEvent)
                closed = true
              case Some(r) if r.secret != secret =>
                sse.send(expiredEvent)
                closed = true
              case Some(r) =>
                // 状态变化才推送，避免重复事件
                if r.status != lastStatus then {
                  lastStatus = r.status
                  val payload = new JsonObject()
                  payload.add("status", r.status)
                  if r.status == QrcodeRecord.Confirmed then payload.add("authToken", r.authToken)
                  sse.send(SseEvent.name(r.status).data(payload.toString))
                  idleSeconds = 0
                  // confirmed/cancelled 为终态，推送后关闭连接
                  if r.status == QrcodeRecord.Confirmed || r.status == QrcodeRecord.Cancelled then closed = true
                }
            }
            if !closed then {
              idleSeconds += 1
              // 每 15 秒心跳一次，保持连接活跃并检测客户端断开
              if idleSeconds >= 15 then {
                sse.ping()
                idleSeconds = 0
              }
              Thread.sleep(1000)
            }
        } catch {
          // 连接断开 / 线程中断：结束推送
          case _: IOException => ()
          case _: InterruptedException => ()
        }
        sse.complete()
    }
    null
  }

  /** GET 扫码确认页（二维码内容，手机端打开）。
   *
   * 未登录时跳转登录页，登录成功后回跳本页；
   * 已登录且应用可识别时标记"已扫码"并渲染确认页。
   */
  def scan(@param("qrcodeId") qrcodeId: String): View = {
    qrcodeService.get(qrcodeId) match {
      case None => toError("二维码不存在或已过期")
      case Some(record) =>
        Securities.session match {
          // 未登录：先登录，登录成功回跳本确认页
          case None =>
            redirect(to("/cas/login", Map("service" -> buildScanUrl(request, qrcodeId))))
          case Some(_) =>
            appInfoProvider.get(record.appName) match {
              // 应用无法识别时视为非法应用，错误页展示具体 service 便于排查
              case None =>
                put("service", record.service)
                toError("非法应用，无法识别的服务地址")
              case Some(app) =>
                qrcodeService.markScanned(qrcodeId)
                csrfDefender.addToken(request, response)
                put("qrcodeId", qrcodeId)
                put("app", app)
                put("user", Securities.user)
                forward()
            }
        }
    }
  }

  /** POST 手机端确认登录。
   *
   * 要求已登录会话与 CSRF 校验，确认后仅生成一次性授权码，不建立任何设备会话。
   */
  @mapping(value = "confirm", methods = "post")
  def confirm(@param("qrcodeId") qrcodeId: String): View = {
    // 写操作先做 CSRF 校验，失败时框架直接返回 403
    if !csrfDefender.valid(request, response) then return null
    qrcodeService.get(qrcodeId) match {
      case None => toError("二维码不存在或已过期")
      case Some(record) =>
        appInfoProvider.get(record.appName) match {
          case None =>
            put("service", record.service)
            toError("非法应用，无法识别的服务地址")
          case Some(app) =>
            // 重复提交时直接展示确认成功页，不重复生成授权码
            if record.status == QrcodeRecord.Confirmed || record.status == QrcodeRecord.Consumed then {
              put("qrcodeId", qrcodeId)
              put("app", app)
              put("user", Securities.user)
              return forward("confirmed")
            }
            Securities.session match {
              case None =>
                redirect(to("/cas/login", Map("service" -> buildScanUrl(request, qrcodeId))))
              case Some(_) =>
                qrcodeService.confirm(qrcodeId, Securities.user)
                put("qrcodeId", qrcodeId)
                put("app", app)
                put("user", Securities.user)
                forward("confirmed")
            }
        }
    }
  }

  /** POST 手机端拒绝登录。
   *
   * 要求已登录会话与 CSRF 校验，拒绝后设备轮询到 cancelled 状态，无法再登录；
   * 拒绝仅结束本次授权，不影响手机端自身登录状态。
   */
  @mapping(value = "cancel", methods = "post")
  def cancel(@param("qrcodeId") qrcodeId: String): View = {
    if !csrfDefender.valid(request, response) then return null
    qrcodeService.get(qrcodeId) match {
      case None => toError("二维码不存在或已过期")
      case Some(record) =>
        appInfoProvider.get(record.appName) match {
          case None =>
            put("service", record.service)
            toError("非法应用，无法识别的服务地址")
          case Some(app) =>
            Securities.session match {
              case None =>
                redirect(to("/cas/login", Map("service" -> buildScanUrl(request, qrcodeId))))
              case Some(_) =>
                if qrcodeService.reject(qrcodeId).isDefined then {
                  put("qrcodeId", qrcodeId)
                  put("app", app)
                  put("user", Securities.user)
                  forward("cancelled")
                } else {
                  // 已被确认/消费/拒绝的二维码无法再次拒绝
                  toError("该请求已被处理，无法拒绝")
                }
            }
        }
    }
  }

  /** GET 设备登录。
   *
   * 设备浏览器访问本接口，CAS 在设备请求上下文建立属于设备的会话，
   * 并按与 LoginAction 一致的语义回跳：请求已携带 sid_name 时传递会话标识、
   * 不签发 ticket；否则签发一次性 service ticket。
   */
  def login(@param("qrcodeId") qrcodeId: String,
            @param("authToken") authToken: String,
            @param("service") service: String): View = {
    if service == null || service.isEmpty || !casService.isValidClient(service) then {
      put("service", service)
      return toError("非法的服务地址，无法登录")
    }
    // 消费一次性授权码，authToken 不匹配或已消费时登录失败
    qrcodeService.consume(qrcodeId, authToken) match {
      case None => toError("授权码无效或已使用，请重新扫码")
      case Some(record) =>
        // 以记录中的用户名建立设备会话，不携带手机端会话上下文
        val session = securityManager.login(request, response, PreauthToken(record.username, null))
        SecurityContext.set(securityContextBuilder.build(request, Some(session)))
        new LoginHelper(securityManager, ticketRegistry, casService)
          .forwardService(request, response, this, service, session)
    }
  }

  /** 渲染错误页，必须携带明确的错误原因。
   *
   *  @param msg 错误原因，错误页展示给用户与排查者
   *  @return error 视图
   */
  private def toError(msg: String): View = {
    put("error", msg)
    forward("error")
  }

  /** 添加跨域响应头：动态回显 Origin 并允许携带凭据，供跨域前端调用 create/status。
   *
   *  与 AuthAction 的处理方式保持一致：仅对携带 Origin 的请求回显该来源。
   */
  private def addCorsHeaders(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val origin = request.getHeader("origin")
    if (Strings.isNotBlank(origin)) {
      response.addHeader("Access-Control-Allow-Origin", origin)
      response.addHeader("Access-Control-Allow-Credentials", "true")
    }
  }

  /** 构造扫码确认页地址，供二维码渲染与登录回跳使用 */
  private def buildScanUrl(request: HttpServletRequest, qrcodeId: String): String = {
    val builder = new UrlBuilder(request.getContextPath)
    builder.serverName = request.getServerName
    builder.port = RequestUtils.getServerPort(request)
    builder.scheme = if (RequestUtils.isHttps(request)) "https" else "http"
    builder.servletPath = "/cas/qrcode/scan"
    builder.queryString = "qrcodeId=" + qrcodeId
    builder.buildUrl()
  }
}
