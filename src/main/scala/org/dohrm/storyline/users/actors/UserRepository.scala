package org.dohrm.storyline.users.actors

import akka.actor.ActorLogging
import org.dohrm.storyline.users.models.{User, UserDatabase}
import org.dohrm.toolkit.actor.response.Response
import org.dohrm.toolkit.actor.{JdbcActor, ModelRepositoryActor}
import org.dohrm.toolkit.context.JdbcConfig

import scala.concurrent.ExecutionContext

/**
  * @author michaeldohr
  * @since 28/05/16
  */
class UserRepository(implicit override val jdbcConfig: JdbcConfig, ec: ExecutionContext)
  extends ModelRepositoryActor[User, String]
    with JdbcActor
    with UserDatabase
    with ActorLogging {

  import jdbcConfig._
  import driver.api._

  implicit val users: TableQuery[Users] = TableQuery[Users]

  override protected def all: Unit = {
    execAndForwardToSender(
      users.result
    )(users => Response(users))
  }

  override protected def one(id: String): Unit = {
    execAndForwardToSender(
      (for {
        u <- users if u.id === id
      } yield u).result
    )(users => Response.ofOpt(users.headOption))
  }

  override protected def create(user: User): Unit = {
    execAndForwardToSender {
      (users returning users) += user
    }(result => Response(result))
  }

  override protected def update(id: String, entity: User): Unit = {
    // forwardToRepository(Update(id, entity))
  }

  override protected def delete(id: String): Unit = {
    // forwardToRepository(Delete(id))
  }

  override def receive: Receive =
    super.receive orElse {
      case _ =>
    }
}
