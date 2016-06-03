package org.dohrm.storyline.users.api

import akka.actor.ActorRef
import akka.util.Timeout
import org.dohrm.storyline.users.models.{User, UserJson}
import org.dohrm.toolkit.api.CrudApi
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

/**
  * @author michaeldohr
  * @since 28/05/16
  */
class CrudUserApi(actorRef: ActorRef)(implicit ec: ExecutionContext, timeout: Timeout, format: RootJsonFormat[User] = UserJson.format)
  extends CrudApi[User](actorRef)
