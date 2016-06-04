package org.dohrm.storyline.users.directives

import akka.actor.ActorRef
import akka.http.scaladsl.server._
import akka.pattern._
import org.dohrm.auth0.directives.Auth0
import org.dohrm.auth0.models.Auth0User
import org.dohrm.storyline.users.actors.GetWithGrants
import org.dohrm.storyline.users.models.{UserWithGrants, User}
import org.dohrm.toolkit.actor.response.Positive
import org.dohrm.toolkit.context.{ActorContext, FutureContext}
import org.joda.time.DateTime

import scala.util.{Failure, Success}


trait UserSecurity {
  self: FutureContext with ActorContext with Auth0 =>

  import akka.http.scaladsl.server.Directives._

  def userRepositoryActor: ActorRef

  private implicit def auth0ToUser(auth0: Auth0User): User = User("", "", "", "", DateTime.now, Some(DateTime.now))

  implicit def authorizedWithGrants: Directive1[UserWithGrants] =
    authorized.flatMap { auth0 =>
      onComplete(userRepositoryActor ? GetWithGrants(auth0)).flatMap {
        case Success(Positive(user: UserWithGrants, _)) =>
          provide(user)
        case Failure(ex) =>
          reject(AuthorizationFailedRejection)
      }
    }

}
