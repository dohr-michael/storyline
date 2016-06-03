package org.dohrm.toolkit.actor

import akka.actor.Actor
import org.dohrm.toolkit.actor.response.{Response, UnknownError}

import scala.reflect.ClassTag


case object GetAll

case class GetOne[ID](id: ID)

case class Create[A](entity: A)

case class Update[A, ID](id: ID, entity: A)

case class Delete[ID](id: ID)


abstract class ModelRepositoryActor[A: ClassTag, ID] extends Actor {

  protected def withClass(entity: Any)(fn: A => Unit): Unit = {
    implicitly[ClassTag[A]]
      .unapply(entity)
      .fold(
        sender ! Response.failed[A](UnknownError)
      ) { entity =>
        fn(entity)
      }
  }

  protected def all: Unit

  protected def one(id: ID): Unit

  protected def create(entity: A): Unit

  protected def update(id: ID, entity: A): Unit

  protected def delete(id: ID): Unit

  override def receive: Receive = {
    case GetAll =>
      all
    case GetOne(id: ID) =>
      one(id)
    case Create(entity) =>
      withClass(entity) { entity =>
        create(entity)
      }
    case Update(id: ID, entity) =>
      withClass(entity) { entity =>
        update(id, entity)
      }
    case Delete(id: ID) =>
      delete(id)
  }
}