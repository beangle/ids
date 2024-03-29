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

package org.beangle.ids.cas

import org.beangle.commons.collection.Collections

import scala.collection.mutable

/** Cas 服务设置
 *
 */
class CasSetting {
  /** 是否登录界面启用验证码 */
  var enableCaptcha: Boolean = _
  /** 登录界面是否显示Local/Remote选项 */
  var displayLoginSwitch: Boolean = _
  /** 防止跨站攻击的key，用于加密生成cookie */
  var key: String = _
  /** 本站的源地址 */
  var origin: String = _
  /** 是否强制使用https */
  var forceHttps: Boolean = _
  /** 是否检查密码强度 */
  var checkPasswordStrength: Boolean = _
  /** 允许的client */
  var clients: mutable.Buffer[String] = Collections.newBuffer[String]
  /** 其他SSO登录地址 */
  var remoteLoginUrl: Option[String] = None
  /** 其他SSO退出地址 */
  var remoteLogoutUrl: Option[String] = None
  /** 本地密码是否是只读 */
  var passwordReadOnly: Boolean = _
  /** 是否启用短信验证码登录 */
  var enableSmsLogin: Boolean = _
}
