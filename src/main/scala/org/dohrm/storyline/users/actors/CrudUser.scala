package org.dohrm.storyline.users.actors

import akka.actor.ActorRef
import org.dohrm.toolkit.actor.CrudActor
import org.dohrm.storyline.users.models.User

/**
  * @author michaeldohr
  * @since 28/05/16
  */
class CrudUser extends CrudActor(classOf[User]) {

  override protected def all(sender: ActorRef): Unit = {
    sender ! Seq.empty
  }

  override protected def one(id: String, sender: ActorRef): Unit = {
    sender ! User("", "", "", "")
  }

  override protected def create(entity: User, sender: ActorRef): Unit = {
    sender ! User("", "", "", "")
  }

  override protected def update(id: String, entity: User, sender: ActorRef): Unit = {
    sender ! User("", "", "", "")
  }

  override protected def delete(id: String, sender: ActorRef): Unit = {
    sender ! Unit
  }
}
