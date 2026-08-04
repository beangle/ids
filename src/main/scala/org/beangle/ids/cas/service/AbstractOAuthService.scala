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

package org.beangle.ids.cas.service

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.cache.redis.RedisCacheManager
import org.beangle.commons.bean.Initializing
import org.beangle.commons.cache.Cache
import org.beangle.commons.io.DefaultBinarySerializer
import org.beangle.commons.json.{Json, JsonObject}
import org.beangle.security.realm.jwt.{JwtDigest, Jwts}
import redis.clients.jedis.RedisClient

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.{Duration, Instant}
import java.util.{Base64, UUID}

/** OAuth2 授权服务抽象实现。
 *
 *  封装授权码生成、PKCE 验证、授权码交换与令牌签发的通用逻辑；
 *  客户端校验、用户资源获取以及授权码验证通过后的会话建立与令牌持久化由子类实现。
 */
abstract class AbstractOAuthService extends OAuthService, Initializing {

  /** 授权码有效期 */
  var codeTTL: Duration = Duration.ofMinutes(5)

  /** 访问令牌有效期 */
  var tokenTTL: Duration = Duration.ofHours(1)

  /** 令牌签名密钥，由子类提供 */
  protected def secret: String

  private var digest: JwtDigest = _
  private[this] var codes: Cache[String, String] = _

  /** 初始化授权码缓存。
   *  @param client Redis 客户端
   */
  protected def initRedis(client: RedisClient): Unit = {
    val cacheManager = new RedisCacheManager(client, new DefaultBinarySerializer, true)
    cacheManager.ttl = codeTTL.getSeconds.toInt
    codes = cacheManager.getCache("oauth2_code", classOf[String], classOf[String])
  }

  override def init(): Unit = {
    this.digest = Jwts.digest(secret)
  }

  /** 生成授权码并存入缓存，强制要求 PKCE。
   *
   *  @param clientId 客户端编码
   *  @param userId 用户编码
   *  @param scope 授权范围，多个用空格分隔
   *  @param codeChallenge PKCE 的 code_challenge，必传
   *  @return 生成的授权码
   */
  override def generateAuthCode(clientId: String, userId: String, scope: String, codeChallenge: String): String = {
    assert(codeChallenge != null && codeChallenge.nonEmpty, "PKCE code_challenge is required")
    val code = UUID.randomUUID().toString.replace("-", "")
    val expiresAt = Instant.now().plusSeconds(codeTTL.getSeconds)
    val oauthCode = new OAuthCode(code, clientId, userId, scope, codeChallenge, expiresAt)
    codes.put(code, OAuthCode.toJson(oauthCode).toJson)
    code
  }

  /** 验证授权码并生成 access token，强制要求 PKCE。
   *
   *  验证通过后授权码将被移除（一次性使用），随后委托 {@link #onCodeValidated} 完成会话建立与令牌签发。
   *
   *  @param code 授权码
   *  @param clientId 客户端编码，需与授权时一致
   *  @param codeVerifier PKCE 的 code_verifier，必传
   *  @return (成功, token或错误信息)
   */
  override def exchangeCode(code: String, clientId: String, codeVerifier: String)
                           (request: HttpServletRequest, response: HttpServletResponse): (Boolean, String) = {
    if (code == null || code.isEmpty || clientId == null || codeVerifier == null || codeVerifier.isEmpty) {
      (false, "Invalid parameters")
    } else {
      val jsonOpt = codes.get(code)
      codes.evict(code)
      if (jsonOpt.isEmpty) {
        (false, "Invalid code")
      } else {
        val oauthCode = OAuthCode.fromJson(Json.parseObject(jsonOpt.get))
        if (Instant.now().isAfter(oauthCode.expiresAt)) {
          (false, "Code expired")
        } else if (clientId != oauthCode.clientId) {
          (false, "Client ID mismatch")
        } else if (!verifyPkceS256(codeVerifier, oauthCode.codeChallenge)) {
          (false, "PKCE verification failed")
        } else {
          onCodeValidated(oauthCode)(request, response)
        }
      }
    }
  }

  /** 授权码验证通过后，由子类完成会话建立与令牌签发持久化。
   *
   *  @param oauthCode 验证通过的授权码
   *  @return (成功, token或错误信息)
   */
  protected def onCodeValidated(oauthCode: OAuthCode)
                               (request: HttpServletRequest, response: HttpServletResponse): (Boolean, String)

  /** 构建 JWT 访问令牌。
   *
   *  @param userId 用户编码
   *  @param clientId 客户端编码
   *  @param scope 授权范围
   *  @param sessionId 建立的会话ID，作为令牌 jti
   */
  protected def buildAccessToken(userId: String, clientId: String, scope: String, sessionId: String): String = {
    val tokenData = new JsonObject()
    tokenData.add("user_id", userId)
    tokenData.add("client_id", clientId)
    tokenData.add("scope", scope)
    tokenData.add("jti", sessionId)
    digest.generateToken(tokenData, tokenTTL)
  }

  /** PKCE S256 验证（RFC 7636） */
  protected def verifyPkceS256(verifier: String, challenge: String): Boolean = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(verifier.getBytes(StandardCharsets.UTF_8))
    val computed = Base64.getUrlEncoder.withoutPadding.encodeToString(hash)
    computed == challenge
  }
}
