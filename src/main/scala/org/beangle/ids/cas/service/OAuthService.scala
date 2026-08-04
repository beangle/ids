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

import java.time.Duration

/** OAuth2 授权服务。
 *
 *  采用 Authorization Code + PKCE 模式（强制要求 PKCE），授权码存放于缓存中。
 */
trait OAuthService {

  /** 生成授权码并存入缓存，强制要求 PKCE */
  def generateAuthCode(clientId: String, userId: String, scope: String, codeChallenge: String): String

  /** 验证授权码并生成 access token，强制要求 PKCE */
  def exchangeCode(code: String, clientId: String, codeVerifier: String)
                  (request: HttpServletRequest, response: HttpServletResponse): (Boolean, String)

  /** 访问令牌有效期 */
  def tokenTTL: Duration

  /** 根据客户端编码查找客户端 */
  def findClient(clientId: String): Option[OAuthClient]

  /** 获取用户可授权资源 */
  def getAuthResources(userId: String): Option[OAuthResources]
}

/** OAuth2 客户端信息 */
case class OAuthClient(id: Any, name: String, redirectUri: String)

/** OAuth2 用户可授权资源 */
case class OAuthResources(user: Any, resources: Iterable[Any])
