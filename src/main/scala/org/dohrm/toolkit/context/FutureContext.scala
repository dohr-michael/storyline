package org.dohrm.toolkit.context

import scala.concurrent.ExecutionContext

/**
  * @author michaeldohr
  * @since 14/05/16
  */
trait FutureContext {

  implicit val ec: ExecutionContext
}
