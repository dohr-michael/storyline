package org.dohrm.storyline.users.api

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directive1
import akka.util.Timeout
import org.dohrm.storyline.users.models.{User, UserJson, UserWithGrants}
import org.dohrm.toolkit.api.CrudApi
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

class CrudUserApi(actorRef: ActorRef)(implicit ec: ExecutionContext, timeout: Timeout, format: RootJsonFormat[User] = UserJson.userFormat, securityDirective: Directive1[UserWithGrants])
  extends CrudApi[User, UserWithGrants](actorRef)
