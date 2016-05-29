package org.dohrm.storyline.users.api

import akka.actor.ActorRef
import akka.util.Timeout
import org.dohrm.toolkit.api.CrudApi
import org.dohrm.storyline.users.models.{User, UserJson}
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

/**
  * @author michaeldohr
  * @since 28/05/16
  */
class CrudUserApi(actorRef: ActorRef)
                 (implicit ec: ExecutionContext, timeout: Timeout)
  extends CrudApi(classOf[User], actorRef)(ec, timeout) {

  override implicit def entityFormat: RootJsonFormat[User] = UserJson.format
}
