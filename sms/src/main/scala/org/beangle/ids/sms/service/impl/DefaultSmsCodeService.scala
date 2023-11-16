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

package org.beangle.ids.sms.service.impl

import org.beangle.commons.lang.Strings
import org.beangle.ids.sms.service.{SmsCacheService, SmsCodeService}
import org.beangle.notify.sms.{Receiver, SmsSender}

import java.util.regex.Pattern

class DefaultSmsCodeService extends SmsCodeService {
  var smsCacheService: SmsCacheService = _

  var smsSender: SmsSender = _

  var template: String = "您的登录验证码为{code}，{ttl}分钟有效!"

  private val mobilePattern = Pattern.compile("^1(3[0-9]|4[01456879]|5[0-3,5-9]|6[2567]|7[0-8]|8[0-9]|9[0-3,5-9])\\d{8}$")

  override def send(receiver: Receiver): String = {
    smsCacheService.get(receiver.mobile) match
      case Some(code) => "验证码已经发送"
      case None =>
        val code = generateCode()
        var contents = Strings.replace(template, "{code}", code)
        contents = Strings.replace(contents, "{ttl}", smsCacheService.ttlMinutes.toString)

        val res = smsSender.send(receiver, contents)
        if (res.isOk) {
          smsCacheService.set(receiver.mobile, code)
          s"验证码成功发送到${receiver.maskMobile}"
        } else {
          "验证码发送失败:" + res.message
        }
  }

  override def verify(mobile: String, code: String): Boolean = {
    val matched = smsCacheService.get(mobile).contains(code)
    if (matched) {
      smsCacheService.remove(mobile)
    }
    matched
  }

  override def validate(mobile: String): Boolean = {
    mobilePattern.matcher(mobile).matches()
  }

  def generateCode(): String = {
    DefaultSmsCodeService.generateDefaultCode()
  }
}

object DefaultSmsCodeService {

  def generateDefaultCode(): String = {
    ((Math.random() * 9 + 1) * 100000).asInstanceOf[Int].toString
  }
}
