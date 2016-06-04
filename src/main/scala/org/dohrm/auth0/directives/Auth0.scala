package org.dohrm.auth0.directives

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.server.{AuthenticationFailedRejection, Directive1, Rejection}
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.dohrm.auth0.actors.Auth0VerifierRequest
import org.dohrm.auth0.models.Auth0User

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * @author michaeldohr
  * @since 04/06/16
  */
trait Auth0 {

  implicit val ec: ExecutionContext
  implicit val as: ActorSystem
  implicit val am: ActorMaterializer
  implicit val actorTimeout: Timeout

  import akka.http.scaladsl.server.Directives._

  def auth0VerifierActor: ActorRef

  private def bearerToken: Directive1[Option[String]] =
    for {
      authBearerHeader <- optionalHeaderValueByName("Authorization").map(extractBearerToken)
      xAuthCookie <- optionalCookie("X-Authorization-Token").map(_.map(_.value))
    } yield authBearerHeader.orElse(xAuthCookie)

  private def extractBearerToken(authHeader: Option[String]): Option[String] =
    authHeader.filter(_.startsWith("Bearer ")).map(token => token.substring("Bearer ".length))

  private def authRejection: Rejection = AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, HttpChallenge("", ""))

  def authorized: Directive1[Auth0User] = {
    bearerToken.flatMap {
      case Some(token) =>
        onComplete((auth0VerifierActor ? Auth0VerifierRequest(token)).mapTo[Auth0User]).flatMap {
          case Success(token: Auth0User) =>
            provide(token)
          case Failure(ex) =>
            //logger.error(ex, "Couldn't log in using provided authorization token")
            reject(authRejection).toDirective[Tuple1[Auth0User]]
          case _ =>
            reject(authRejection).toDirective[Tuple1[Auth0User]]
        }
      case None =>
        reject(authRejection)
    }
  }

}
