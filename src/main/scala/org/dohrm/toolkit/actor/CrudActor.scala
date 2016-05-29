package org.dohrm.toolkit.actor

import akka.actor.{Actor, ActorRef}
import org.dohrm.toolkit.actor.CrudActor.{All, Create, Delete, One, Update}


case object FindAll


abstract class CrudActor[A](val clazz: Class[A]) extends Actor {

  type Response[AA] = Response.Response[AA]

  protected def all(sender: ActorRef): Unit

  protected def one(id: String, sender: ActorRef): Unit

  protected def create(entity: A, sender: ActorRef): Unit

  protected def update(id: String, entity: A, sender: ActorRef): Unit

  protected def delete(id: String, sender: ActorRef): Unit

  override def receive: Receive = {
    case All => all(sender())
    case One(id) => one(id, sender())
    case Create(entity) if clazz.isInstance(entity) => create(entity.asInstanceOf[A], sender())
    case Update(id: String, entity) if clazz.isInstance(entity) => update(id, entity.asInstanceOf[A], sender())
    case Delete(id: String) => delete(id, sender())
  }
}


object CrudActor {

  case object All

  case class One(id: String)

  case class Create[A](entity: A)

  case class Update[A](id: String, entity: A)

  case class Delete(id: String)

}