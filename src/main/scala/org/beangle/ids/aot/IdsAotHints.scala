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

package org.beangle.ids.aot

import org.beangle.commons.aot.AotHintRegistrar
import org.beangle.ids.cas.*

/** beangle-ids 的 GraalVM native-image 反射提示。 */
class IdsAotHints extends AotHintRegistrar {
  override def registering(): Unit = {
    hints.registerType(
      classOf[id.ServiceTicketIdGenerator],
      classOf[service.AbstractOAuthService],
      classOf[service.CasAppInfoProvider],
      classOf[service.CasService],
      classOf[service.LoginRetryService],
      classOf[service.OAuthService],
      classOf[service.QrcodeService],
      classOf[service.Services],
      classOf[service.UserMobileProvider],
      classOf[ticket.DefaultServiceTicket],
      classOf[ticket.TicketCacheService],
      classOf[ticket.TicketRegistry])
    hints.registerType(classOf[service.UserMobileProvider.type])
  }
}
