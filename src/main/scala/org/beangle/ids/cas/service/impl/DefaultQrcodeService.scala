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

package org.beangle.ids.cas.service.impl

import org.beangle.cache.redis.RedisCacheManager
import org.beangle.commons.bean.Initializing
import org.beangle.commons.cache.Cache
import org.beangle.commons.io.DefaultBinarySerializer
import org.beangle.ids.cas.CasSetting
import org.beangle.ids.cas.id.impl.DefaultRandomStringGenerator
import org.beangle.ids.cas.service.QrcodeService
import org.beangle.ids.cas.ticket.QrcodeRecord
import redis.clients.jedis.RedisClient

/** 基于 Redis 缓存的二维码服务实现。
 *
 *  二维码记录存放在独立缓存 cas_qrcodes 中，不落数据库；
 *  缓存 TTL 由 {@link CasSetting#qrcodeExpireSeconds} 决定（默认 180 秒）。
 */
class DefaultQrcodeService extends QrcodeService, Initializing {

  /** CAS 全局设置，qrcodeExpireSeconds 决定二维码记录的实际存活时长 */
  var casSetting: CasSetting = _

  private[this] var records: Cache[String, QrcodeRecord] = _

  private var client: RedisClient = _

  def this(client: RedisClient) = {
    this()
    this.client = client
  }

  private val tokenGenerator = new DefaultRandomStringGenerator(48)

  /** 依赖注入完成后创建缓存，TTL 取自 casSetting.qrcodeExpireSeconds */
  override def init(): Unit = {
    val serializer = new DefaultBinarySerializer
    serializer.registerClass(classOf[QrcodeRecord])
    val cacheManager = new RedisCacheManager(client, serializer, true)
    cacheManager.ttl = casSetting.qrcodeExpireSeconds
    records = cacheManager.getCache("cas_qrcodes", classOf[String], classOf[QrcodeRecord])
  }

  override def create(qrcodeId: String, secret: String, service: String, appName: String, deviceIp: String, ttlSeconds: Int): QrcodeRecord = {
    val record = new QrcodeRecord
    record.qrcodeId = qrcodeId
    record.secret = secret
    record.service = service
    record.appName = appName
    record.deviceIp = deviceIp
    record.expireAt = System.currentTimeMillis / 1000 + ttlSeconds
    records.put(qrcodeId, record)
    record
  }

  override def get(qrcodeId: String): Option[QrcodeRecord] = {
    // 过期判定完全依赖 Redis key 的 TTL（写入时 setex 180 秒），
    // 不做额外的逻辑过期检查，避免与物理 TTL 不同步导致"已失效但 key 仍在"
    if qrcodeId == null then None
    else records.get(qrcodeId)
  }

  override def markScanned(qrcodeId: String): Option[QrcodeRecord] = {
    get(qrcodeId) match {
      case Some(record) if record.status == QrcodeRecord.Pending =>
        record.status = QrcodeRecord.Scanned
        records.put(qrcodeId, record)
        Some(record)
      case other => other
    }
  }

  override def confirm(qrcodeId: String, username: String): Option[QrcodeRecord] = {
    get(qrcodeId) match {
      case Some(record) if record.status == QrcodeRecord.Pending || record.status == QrcodeRecord.Scanned =>
        record.status = QrcodeRecord.Confirmed
        record.username = username
        record.authToken = tokenGenerator.nextString()
        records.put(qrcodeId, record)
        Some(record)
      case other => other
    }
  }

  override def reject(qrcodeId: String): Option[QrcodeRecord] = {
    get(qrcodeId) match {
      case Some(record) if record.status == QrcodeRecord.Confirmed || record.status == QrcodeRecord.Consumed =>
        None
      case Some(record) =>
        record.status = QrcodeRecord.Cancelled
        records.put(qrcodeId, record)
        Some(record)
      case None => None
    }
  }

  override def consume(qrcodeId: String, authToken: String): Option[QrcodeRecord] = {
    get(qrcodeId) match {
      case Some(record) if record.status == QrcodeRecord.Confirmed && record.authToken == authToken =>
        record.status = QrcodeRecord.Consumed
        records.put(qrcodeId, record)
        Some(record)
      case _ => None
    }
  }

  override def evict(qrcodeId: String): Unit = records.evict(qrcodeId)
}
