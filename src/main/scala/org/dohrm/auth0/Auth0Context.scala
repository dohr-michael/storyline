package org.dohrm.auth0

import akka.actor.{ActorRef, Props}
import com.typesafe.config.Config
import org.dohrm.auth0.actors.Auth0Verifier
import org.dohrm.auth0.directives.Auth0

/**
  * @author michaeldohr
  * @since 04/06/16
  */
trait Auth0Context extends Auth0 {
  implicit val config: Config

  override val auth0VerifierActor: ActorRef = as.actorOf(
    Props(
      new Auth0Verifier(
        config.getString("auth0.clientId"),
        config.getString("auth0.clientSecret"),
        config.getString("auth0.domain")
      )
    )
  )
}
