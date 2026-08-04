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

/** 扫码登录页面展示的应用信息 */
case class CasAppInfo(name: String, logo: Option[String] = None, home: Option[String] = None)

/** 扫码登录应用信息提供者。
 *
 *  由部署方按自身应用注册信息实现，例如依据 ems 中 app 注册的 name 匹配。
 */
trait CasAppInfoProvider {

  /** 根据应用名称获取应用信息
   *  @param name 应用名称（创建设备传入）
   *  @return 应用信息，无法识别时返回 None
   */
  def get(name: String): Option[CasAppInfo]
}
