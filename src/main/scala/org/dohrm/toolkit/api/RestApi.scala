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
import org.dohrm.toolkit.actor.response._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag


trait RestApi extends DefaultJsonProtocol with SprayJsonSupport {

  import spray.json._

  def causeMarshaller: PartialFunction[Explanation, StatusCode] = PartialFunction.empty

  def defaultCauseMarshaller: PartialFunction[Explanation, StatusCode] = {
    case ObjectCreated => StatusCodes.Created
    case _ => StatusCodes.OK
  }

  def causeHeader: PartialFunction[Explanation, immutable.Seq[HttpHeader]] = PartialFunction.empty

  def defaultCauseHeader: PartialFunction[Explanation, immutable.Seq[HttpHeader]] = {
    case _ => Nil
  }

  def errorMarshaller: PartialFunction[response.Error, ToResponseMarshallable] = PartialFunction.empty

  def defaultErrorMarshaller: PartialFunction[response.Error, ToResponseMarshallable] = {
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

  implicit def errorToResponse(error: response.Error): ToResponseMarshallable =
    (errorMarshaller orElse defaultErrorMarshaller) (error)

  implicit def explanationToStatus(explanation: Option[Explanation]): StatusCode =
    explanation.fold[StatusCode](StatusCodes.OK) { c =>
      (causeMarshaller orElse defaultCauseMarshaller) (c)
    }

  implicit def explanationToHeader(explanation: Option[Explanation]): immutable.Seq[HttpHeader] =
    explanation.fold[immutable.Seq[akka.http.scaladsl.model.HttpHeader]](Nil) { c =>
      (causeHeader orElse defaultCauseHeader) (c)
    }

  implicit def entityToResponse[A](entity: Response[A])(implicit wrt: RootJsonFormat[A]): ToResponseMarshallable =
    entity match {
      case Positive(e, a) =>
        HttpResponse(explanationToStatus(a), explanationToHeader(a), e.toJson.toString): ToResponseMarshallable
      case Negative(n) =>
        errorToResponse(n)
    }

  implicit def unitToResponse(implicit stc: StatusCode = StatusCodes.OK): ToResponseMarshallable = {
    HttpResponse(stc, Nil, HttpEntity.Empty)
  }

  implicit class Enrichment(future: Future[Any]) {

    private def errorHandler(implicit ec: ExecutionContext): PartialFunction[Throwable, ToResponseMarshallable] = {
      case e: ClassCastException =>
        future.map {
          case er: response.Error =>
            println("map to error")
            (errorMarshaller orElse defaultErrorMarshaller) (er)
          case e =>
            println(e) // TODO add logger
            HttpResponse(StatusCodes.InternalServerError): ToResponseMarshallable
        }
    }

    def to[A](implicit ec: ExecutionContext, wrt: RootJsonFormat[A], tag: ClassTag[A], stc: StatusCode = StatusCodes.OK): ToResponseMarshallable = {
      future
        .mapTo[Response[A]]
        .map(a => a: ToResponseMarshallable)
        .recover {
          errorHandler
        }
    }

    def toUnit(implicit ec: ExecutionContext, tag: ClassTag[Unit], stc: StatusCode = StatusCodes.OK): ToResponseMarshallable = {
      future
        .mapTo[Unit]
        .map(_ => unitToResponse)
        .recover {
          errorHandler
        }
    }

  }

}


abstract class CrudApi[A: ClassTag](val actorRef: ActorRef)
                                   (implicit ec: ExecutionContext, timeout: Timeout, entityFormat: RootJsonFormat[A]) extends RestApi {

  val routes: Route =
    path(Segment) { id =>
      get {
        complete((actorRef ? GetOne(id)).to[A])
      } ~
        put {
          entity(as[A]) { item =>
            complete((actorRef ? Update(id, item)).to[A])
          }
        } ~
        delete {
          complete((actorRef ? Delete(id)).toUnit)
        }
    } ~
      get {
        complete((actorRef ? GetAll).to[Seq[A]])
      } ~
      post {
        entity(as[A]) { item =>
          complete((actorRef ? Create(item)).to[A])
        }
      }
}