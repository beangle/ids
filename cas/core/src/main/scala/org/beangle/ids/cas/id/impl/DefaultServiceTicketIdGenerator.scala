package org.beangle.ids.cas.id.impl

import org.beangle.ids.cas.id.ServiceTicketIdGenerator

/**
 * @author chaostone
 */
class DefaultServiceTicketIdGenerator extends DefaultIdGenerator("ST-", 35) with ServiceTicketIdGenerator 
