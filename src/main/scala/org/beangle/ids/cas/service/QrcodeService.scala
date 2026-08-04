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

import org.beangle.ids.cas.ticket.QrcodeRecord

/** 扫码登录二维码服务。
 *
 *  二维码票据全程存放于缓存中，状态机：pending → scanned → confirmed → consumed；pending/scanned/confirmed(未消费) → cancelled
 */
trait QrcodeService {

  /** 创建二维码记录。
   *  @param qrcodeId 高熵随机标识
   *  @param secret 设备轮询密钥，仅创建设备知晓
   *  @param service 登录成功后的跳转服务地址
   *  @param appName 应用名称，确认页据此识别应用
   *  @param deviceIp 创建设备的IP
   *  @param ttlSeconds 有效期，单位秒
   */
  def create(qrcodeId: String, secret: String, service: String, appName: String, deviceIp: String, ttlSeconds: Int): QrcodeRecord

  /** 获取记录，过期自动清除。
   *  @return 记录，不存在或已过期返回 None
   */
  def get(qrcodeId: String): Option[QrcodeRecord]

  /** 标记已扫码，仅 pending 状态可转换。
   *  @return 更新后的记录
   */
  def markScanned(qrcodeId: String): Option[QrcodeRecord]

  /** 手机确认登录，生成一次性授权码并将状态置为 confirmed。
   *  @return 更新后的记录
   */
  def confirm(qrcodeId: String, username: String): Option[QrcodeRecord]

  /** 手机拒绝登录，pending/scanned/confirmed(未消费) 状态可转为 cancelled。
   *  @return 更新后的记录，已消费或不存在返回 None
   */
  def reject(qrcodeId: String): Option[QrcodeRecord]

  /** 设备凭授权码消费记录，成功后将状态置为 consumed（一次性）。
   *  @return 记录，授权码不匹配或已消费返回 None
   */
  def consume(qrcodeId: String, authToken: String): Option[QrcodeRecord]

  /** 移除记录 */
  def evict(qrcodeId: String): Unit
}
