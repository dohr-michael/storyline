package org.dohrm.toolkit.api

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import org.dohrm.toolkit.actor._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag


trait RestApi extends DefaultJsonProtocol with SprayJsonSupport {

  import spray.json._

  type Response[A] = Response.Response[A]

  def errorMarshaller: PartialFunction[Error, ToResponseMarshallable] = PartialFunction.empty

  def defaultErrorMarshaller: PartialFunction[Error, ToResponseMarshallable] = {
    case NotFoundError(messages) =>
      HttpResponse(StatusCodes.NotFound, Nil, HttpEntity.apply(JsObject("errors" -> messages.toJson).toString))
    case InvalidRequestError(messages) =>
      HttpResponse(StatusCodes.BadRequest, Nil, HttpEntity.apply(JsObject("errors" -> messages.toJson).toString))
    case ExceptionError(e) =>
      println(e) // TODO add logger.
      HttpResponse(StatusCodes.InternalServerError)
    case _ =>
      HttpResponse(StatusCodes.InternalServerError)
  }

  implicit def errorToResponse(error: Error): ToResponseMarshallable = {
    (errorMarshaller orElse defaultErrorMarshaller) (error)
  }

  implicit def entityToResponse[A](entity: A)(implicit wrt: RootJsonFormat[A], stc: StatusCode = StatusCodes.OK): ToResponseMarshallable = {
    HttpResponse(stc, Nil, entity.toJson.toString)
  }

  implicit def unitToResponse(implicit stc: StatusCode = StatusCodes.OK): ToResponseMarshallable = {
    HttpResponse(stc, Nil, HttpEntity.Empty)
  }

  implicit class Enrichment(future: Future[Any]) {

    private def errorHandler(implicit ec: ExecutionContext): PartialFunction[Throwable, ToResponseMarshallable] = {
      case e: ClassCastException =>
        future.map {
          case er: Error => (errorMarshaller orElse defaultErrorMarshaller) (er)
          case e =>
            println(e) // TODO add logger
            HttpResponse(StatusCodes.InternalServerError): ToResponseMarshallable
        }
    }

    def to[A](implicit ec: ExecutionContext, wrt: RootJsonFormat[A], tag: ClassTag[A], stc: StatusCode = StatusCodes.OK): ToResponseMarshallable = {
      try {
        future.mapTo[A]
      } catch errorHandler
    }

    def toUnit(implicit ec: ExecutionContext, tag: ClassTag[Unit], stc: StatusCode = StatusCodes.OK): ToResponseMarshallable = {
      try {
        future.mapTo[Unit].map(_ => unitToResponse)
      } catch errorHandler
    }

  }

}


abstract class CrudApi[A](val clazz: Class[A], val actorRef: ActorRef)
                         (implicit ec: ExecutionContext, timeout: Timeout) extends RestApi {

  private implicit val classTag: ClassTag[A] = ClassTag(clazz)
  implicit def entityFormat: RootJsonFormat[A]

  val routes: Route =
    get {
      complete((actorRef ? CrudActor.All).to[Seq[A]])
    } ~
      post {
        entity(as[A]) { item =>
          complete((actorRef ? CrudActor.Create).to[A])
        }
      } ~
      path(Segment) { id =>
        get {
          complete((actorRef ? CrudActor.One(id)).to[A])
        } ~
          put {
            entity(as[A]) { item =>
              complete((actorRef ? CrudActor.Update(id, item)).to[A])
            }
          } ~
          delete {
            complete((actorRef ? CrudActor.Delete(id)).toUnit)
          }
      }
}