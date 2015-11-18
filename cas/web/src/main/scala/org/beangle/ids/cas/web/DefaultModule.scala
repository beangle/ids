package org.beangle.ids.cas.web

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.ids.cas.web.action.LoginAction
import org.beangle.ids.cas.web.action.LogoutAction
import org.beangle.ids.cas.web.action.ServiceValidateAction

/**
 * @author chaostone
 */
class DefaultModule extends AbstractBindModule {

  override def binding() {
    bind(classOf[LoginAction])
    bind(classOf[ServiceValidateAction])
    bind(classOf[LogoutAction])
  }

}