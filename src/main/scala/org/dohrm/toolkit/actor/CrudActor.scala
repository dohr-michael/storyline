package org.dohrm.toolkit.actor

import akka.actor.Actor
import org.dohrm.toolkit.actor.response.{Response, UnknownError}

import scala.reflect.ClassTag


case object GetAll

case class GetOne(id: String)

case class Create[A](entity: A)

case class Update[A](id: String, entity: A)

case class Delete(id: String)


abstract class CrudActor[A: ClassTag] extends Actor {

  protected def all: Unit

  protected def one(id: String): Unit

  protected def create(entity: A): Unit

  protected def update(id: String, entity: A): Unit

  protected def delete(id: String): Unit

  protected def withClass(entity: Any)(fn: A => Unit): Unit = {
    implicitly[ClassTag[A]]
      .unapply(entity)
      .fold(
        sender ! Response.failed[A](UnknownError)
      ) { entity =>
        fn(entity)
      }
  }

  override def receive: Receive = {
    case GetAll =>
      all
    case GetOne(id) =>
      one(id)
    case Create(entity) =>
      withClass(entity) { entity =>
        create(entity)
      }
    case Update(id: String, entity) =>
      withClass(entity) { entity =>
        update(id, entity)
      }
    case Delete(id: String) =>
      delete(id)
  }
}