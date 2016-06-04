package org.dohrm.storyline.users.api

import akka.actor.ActorRef
import akka.http.scaladsl.server.{Directive1, Route}
import akka.util.Timeout
import org.dohrm.auth0.models.Auth0User
import org.dohrm.storyline.users.models.{User, UserJson}
import org.dohrm.toolkit.api.CrudApi
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

/**
  * @author michaeldohr
  * @since 28/05/16
  */
class CrudUserApi(actorRef: ActorRef, authorized: Directive1[Auth0User])(implicit ec: ExecutionContext, timeout: Timeout, format: RootJsonFormat[User] = UserJson.userFormat)
  extends CrudApi[User](actorRef) {
  override val routes: Route =
    authorized { user =>
      super.routes
    }
}
