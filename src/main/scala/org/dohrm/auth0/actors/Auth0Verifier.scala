package org.dohrm.auth0.actors

import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.auth0.jwt.JWTVerifier
import org.apache.commons.codec.binary.Base64
import org.dohrm.auth0.models.Auth0User

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}


case class Auth0VerifierRequest(jwt: String)

class Auth0Verifier(val clientId: String, val clientSecret: String, val domain: String)
                   (implicit am: ActorMaterializer, as: ActorSystem, ec: ExecutionContext)
  extends Actor
    with ActorLogging {

  import SprayJsonSupport._
  import spray.json.DefaultJsonProtocol._
  import spray.json._


  lazy val verifier: JWTVerifier = new JWTVerifier(new Base64(true).decode(clientSecret), clientId)

  private lazy val TokenInfoUri = s"https://$domain/tokeninfo"

  def jwtVerifier(token: String): Unit = {
    Try(verifier.verify(s"$token")) match {
      case Success(props) =>
        getTokenInfo(token)
      case Failure(ex) =>
        log.error("Failed to validate bearer token", ex)
        sender ! ex
    }
  }

  def getTokenInfo(token: String) = {
    val currentSender = sender
    (for {
      entity <- Marshal(JsObject("id_token" -> JsString(token))).to[RequestEntity]
      response <- Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = TokenInfoUri, entity = entity))
      entity <- Unmarshal(response.entity).to[JsObject]
    } yield entity)
      .map { obj =>
        obj.getFields("name", "email", "user_id", "locale", "picture") match {
          case Seq(JsString(name), JsString(email), JsString(userId), JsString(locale), JsString(picture)) =>
            currentSender ! Auth0User(userId, email, name, locale, picture)
          case _ =>
            log.error("failed to read token")
            currentSender ! new RuntimeException("Failed to read token")
        }
      }
      .recover {
        case ex =>
          log.error("Failed to retrieve token info : ", ex)
          currentSender ! ex
      }
  }

  override def receive: Receive = {
    case Auth0VerifierRequest(jwt) =>
      jwtVerifier(jwt)
  }
}
