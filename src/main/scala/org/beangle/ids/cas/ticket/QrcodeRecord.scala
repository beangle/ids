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

package org.beangle.ids.cas.ticket

import java.io.{Externalizable, ObjectInput, ObjectOutput}

/** 扫码登录二维码记录，全程存放于缓存中，不落数据库。
 *
 *  状态机：pending → scanned → confirmed → consumed；pending/scanned/confirmed(未消费) → cancelled。
 *  过期判定完全依赖 Redis key 的物理 TTL（由 CasSetting.qrcodeExpireSeconds 配置，默认 180 秒），expireAt 仅作展示用。
 */
class QrcodeRecord extends Externalizable {

  var qrcodeId: String = _
  var secret: String = _
  var service: String = _
  /** 应用名称，确认页据此识别应用 */
  var appName: String = _
  var status: String = QrcodeRecord.Pending
  var username: String = _
  var authToken: String = _
  var deviceIp: String = _
  var expireAt: Long = _

  def writeExternal(out: ObjectOutput): Unit = {
    out.writeObject(qrcodeId)
    out.writeObject(secret)
    out.writeObject(service)
    out.writeObject(appName)
    out.writeObject(status)
    out.writeObject(username)
    out.writeObject(authToken)
    out.writeObject(deviceIp)
    out.writeLong(expireAt)
  }

  def readExternal(in: ObjectInput): Unit = {
    qrcodeId = in.readObject.asInstanceOf[String]
    secret = in.readObject.asInstanceOf[String]
    service = in.readObject.asInstanceOf[String]
    appName = in.readObject.asInstanceOf[String]
    status = in.readObject.asInstanceOf[String]
    username = in.readObject.asInstanceOf[String]
    authToken = in.readObject.asInstanceOf[String]
    deviceIp = in.readObject.asInstanceOf[String]
    expireAt = in.readLong
  }
}

object QrcodeRecord {
  val Pending = "pending"
  val Scanned = "scanned"
  val Confirmed = "confirmed"
  val Cancelled = "cancelled"
  val Consumed = "consumed"
}
