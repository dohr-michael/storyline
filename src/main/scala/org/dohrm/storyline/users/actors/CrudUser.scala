package org.dohrm.storyline.users.actors

import akka.actor.{Props, ActorLogging, ActorPath, ActorRef}
import org.dohrm.toolkit.actor.CrudActor
import org.dohrm.storyline.users.models.User

/**
  * @author michaeldohr
  * @since 28/05/16
  */
class CrudUser extends CrudActor[User] with ActorLogging {

  def forwardToRepository(message: Any) = {
    context.system.actorSelection("*/user-repository").forward(message)
  }

  override protected def all: Unit = {
    forwardToRepository(Seq.empty)
  }

  override protected def one(id: String): Unit = {
    forwardToRepository(User("admin", "admin", "Super", "Admin"))
  }

  override protected def create(entity: User): Unit = {
    forwardToRepository(User("admin", "admin", "Super", "Admin"))
  }

  override protected def update(id: String, entity: User): Unit = {
    forwardToRepository(User("admin", "admin", "Super", "Admin"))
  }

  override protected def delete(id: String): Unit = {
    forwardToRepository(Unit)
  }
}
