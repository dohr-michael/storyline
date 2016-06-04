package org.dohrm.toolkit.actor

import akka.actor.Actor
import org.dohrm.toolkit.actor.response.{Response, UnknownError}
import org.dohrm.toolkit.security.models.SecurityUser

import scala.reflect.ClassTag


case class GetAll[SU <: SecurityUser](securityUser: Option[SU] = None)

case class GetOne[ID, SU <: SecurityUser](id: ID, securityUser: Option[SU] = None)

case class Create[A, SU <: SecurityUser](entity: A, securityUser: Option[SU] = None)

case class Update[A, ID, SU <: SecurityUser](id: ID, entity: A, securityUser: Option[SU] = None)

case class Delete[ID, SU <: SecurityUser](id: ID, securityUser: Option[SU] = None)


abstract class ModelRepositoryActor[A: ClassTag, ID: ClassTag, SU <: SecurityUser : ClassTag] extends Actor {

  protected def getAs[TYPE](entity: Any)(implicit tag: ClassTag[TYPE]): Response[TYPE] = {
    tag.unapply(entity).fold(Response.failed[TYPE](UnknownError))(Response.unit)
  }

  protected def getAs[TYPE](entity: Option[Any])(implicit tag: ClassTag[TYPE]): Response[Option[TYPE]] = {
    entity.fold[Response[Option[TYPE]]](Response.unit(None)) { obj =>
      getAs[TYPE](obj).map(e => Some(e))
    }
  }

  protected def all(implicit su: Option[SU] = None): Unit

  protected def one(id: ID)(implicit su: Option[SU] = None): Unit

  protected def create(entity: A)(implicit su: Option[SU] = None): Unit

  protected def update(id: ID, entity: A)(implicit su: Option[SU] = None): Unit

  protected def delete(id: ID)(implicit su: Option[SU] = None): Unit

  override def receive: Receive = {
    case GetAll(_1: Option[_]) =>
      (for {
        su <- getAs[SU](_1)
      } yield all(su)).recover {
        case e => sender ! Response.failed(e)
      }
    case GetOne(_1, _2: Option[_]) =>
      (for {
        id <- getAs[ID](_1)
        su <- getAs[SU](_2)
      } yield one(id)(su)).recover {
        case e => sender ! Response.failed(e)
      }
    case Create(_1, _2: Option[_]) =>
      (for {
        entity <- getAs[A](_1)
        su <- getAs[SU](_2)
      } yield create(entity)(su)).recover {
        case e => sender ! Response.failed(e)
      }
    case Update(_1, _2, _3: Option[_]) =>
      (for {
        id <- getAs[ID](_1)
        entity <- getAs[A](_2)
        su <- getAs[SU](_3)
      } yield update(id, entity)(su)).recover {
        case e => sender ! Response.failed(e)
      }
    case Delete(_1, _2: Option[_]) =>
      (for {
        id <- getAs[ID](_1)
        su <- getAs[SU](_2)
      } yield delete(id)(su)).recover {
        case e => sender ! Response.failed(e)
      }
  }
}