package org.dohrm.toolkit.context

import com.typesafe.config.Config

/**
  * @author michaeldohr
  * @since 14/05/16
  */
trait ConfigContext {

  implicit val config:Config

}
