package org.dohrm.toolkit.context

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout

/**
  * @author michaeldohr
  * @since 14/05/16
  */
trait ActorContext {
  implicit val as: ActorSystem
  implicit val am: ActorMaterializer
  implicit val actorTimeout: Timeout
}
